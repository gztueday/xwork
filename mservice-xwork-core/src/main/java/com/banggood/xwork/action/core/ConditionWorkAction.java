package com.banggood.xwork.action.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.banggood.xwork.action.condition.ConditionCompare;
import com.banggood.xwork.action.function.FunctionDescriptor;
import com.banggood.xwork.core.common.FunctionUtils;
import com.banggood.xwork.core.common.FunctionUtils.MatchType;
import com.banggood.xwork.core.exception.MatchException;

/**
 * 条件类动作
 */
@WorkActionTag(desc="根据判断父节点输出的参数决定执行子节点",name="条件结点")
public class ConditionWorkAction extends WorkActionBase {

	/*
	 * 
	 * format:<child action name,List<List<RouteCondition>>
	 * List<List<RouteCondition> :一个List<RouteCondition为一个and关系组
	 */
	private Map<String, List<List<RouteCondition>>> condition = new HashMap<String, List<List<RouteCondition>>>();

	/**
	 * 参数配置，每运行一个实例时需要解析这配置，生成实际运行的参数数据
	 */
	private Map<String, String> paramConfig = new HashMap<String, String>();

	private final static Logger logger = LoggerFactory.getLogger(ConditionWorkAction.class);
	
	


	public ConditionWorkAction() throws NumberFormatException, MatchException {
		super( WorkActionType.CONDITIONACTION);

		Iterator<Entry<String, String>> iter = paramConfig.entrySet().iterator();

		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			String command = entry.getValue();
			// 解析配置函数
			if (FunctionUtils.checkParamConfig(command)==MatchType.MATCHFUNCTION) {
				FunctionDescriptor fd = FunctionUtils.matchFunction(command);
				String value = FunctionUtils.invokeTimeFunction(fd);
				this.putLong(entry.getKey(), Long.parseLong(value));
			} else {
				// 解析变量，变量只能由父节点传进来
				String variableKey = FunctionUtils.matchVariable(command);
				// 长度为3，format:[workFlowName,actionName,key]
				String[] actionKey = variableKey.split(":");
				boolean find = false;
				for (WorkActionBase father : this.listFatherActions()) {
					if (father.getActionName().equals(actionKey[1]) && father.getFlowName().equals(actionKey[0])) {
						find = true;
						if (father.getOutPut().containsKey(actionKey[2])) {
							this.getRunParam().putString(entry.getKey(), this.getOutPut().get(actionKey[2]).toString());
						} else {
							throw new RuntimeException("find param config from father:" + father.getFormatName()
									+ " but father's output params  does not have the key:" + actionKey[2]);
						}
					}
				}

				if (find) {
					throw new RuntimeException(
							"can not find param config from father:" + actionKey[0] + "_" + actionKey[1]);
				}
			}
		}
	}

	@Override
	public Map<String, Object> getOutPut() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addRelation(String childActionName, RouteCondition... routeConditions) {
		if (condition.containsKey(childActionName)) {
			List<List<RouteCondition>> andRelations = condition.get(childActionName);
			List<RouteCondition> newAndRelation = new ArrayList<RouteCondition>();
			for (RouteCondition condition : routeConditions) {
				newAndRelation.add(condition);
			}

			andRelations.add(newAndRelation);

			logger.info("add and relation size:" + newAndRelation.size());

		} else {

			List<List<RouteCondition>> andRelations = new ArrayList<List<RouteCondition>>();

			this.condition.put(childActionName, andRelations);
			List<RouteCondition> newAndRelation = new ArrayList<RouteCondition>();
			for (RouteCondition condition : routeConditions) {
				newAndRelation.add(condition);
			}

			andRelations.add(newAndRelation);

			logger.info("add and relation size:" + newAndRelation.size());
		}

	}

	@Override
	public void kill() {
		// TODO Auto-generated method stub

	}

	@Override
	public void fail() {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeStart() {
		// TODO Auto-generated method stub
		

	}

	@Override
	protected void not2do() {

	}

	@Override
	protected void close() {

	}

	@Override
	public void putActionsArguments(Map<String, String> argumentMaps) {

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void execute() throws InterruptedException {
		// TODO Auto-generated method stub
		List<WorkActionBase> matchedChildren = new ArrayList<WorkActionBase>();
		for (WorkActionBase child : this.listChildActions()) {
			String cname = child.getActionName();
			List<List<RouteCondition>> conditions = this.condition.get(cname);
			if (condition != null) {
				// or 关系
				for (List<RouteCondition> condition : conditions) {
					boolean matched = false;
					// 只要其中一个条件不满足便可以跳出，and 关系
					int andMatchCount = 0;
					for (RouteCondition rc : condition) {
						String fname = rc.getFatherActionName();
						boolean find = false;
						for (WorkActionBase father : this.listFatherActions()) {

							if (father.getActionName().equals(fname)) {
								Map<String, Object> output = father.getOutPut();

								if (output == null) {
									find = false;
									logger.error("father action:" + father.getFormatName()
											+ "does has any output,work flow will be kill by itselt");
									this.fail();
									break;
								}
								if (output.containsKey(rc.getCondition().getColumnKey())) {
									Object obj = output.get(rc.getCondition().getColumnKey());
									ConditionCompare cc = new ConditionCompare(rc.getCondition().getColumnKey(), obj);
									if (!rc.compare(cc)) {
										find = false;
										break;
									} else {
										andMatchCount++;
										find = true;
									}
								} else {
									if (rc.isRequired()) {
										logger.warn("does not have param :" + rc.getCondition().getColumnKey()
												+ " in father:" + father.getFormatName() + " it be will be fail");
										this.fail();
										find = false;
										break;
									}
								}

							}
						}

						if (!find) {
							matched = false;
							break;
						} else {
							if (andMatchCount == condition.size()) {
								matched = true;
							}
						}

					}
					if (matched) {
						matchedChildren.add(child);
					} else {
						child.doChangeConditionPass(true);
					}
				}

			} else {
				matchedChildren.add(child);
				logger.info("default child :" + child.getFormatName());
			}

		}
		// 将其子节点依赖的conditionpass设为false
		for (WorkActionBase child : matchedChildren) {
			child.doChangeConditionPass(false);
		}

		for (WorkActionBase child : matchedChildren) {
			child.statusTransfer(child, WorkFlowEvent.SUBMIT);
		}

	}

	@Override
	public String getLog() {
		return null;
	}

	public static class RouteCondition {
		private String fatherActionName;

		private ConditionCompare condition;

		private boolean required = false;

		public RouteCondition(String fatherActionName, ConditionCompare condition) {
			this.fatherActionName = fatherActionName;
			this.condition = condition;
		}

		public RouteCondition(String fatherActionName, ConditionCompare condition, boolean required) {
			this.fatherActionName = fatherActionName;
			this.condition = condition;
			this.required = required;
		}

		public String getFatherActionName() {
			return fatherActionName;
		}

		public void setFatherActionName(String fatherName) {
			this.fatherActionName = fatherName;
		}

		public boolean compare(ConditionCompare condition) {
			return this.condition.compareObjct(condition);
		}

		public ConditionCompare getCondition() {
			return condition;
		}

		public void setCondition(ConditionCompare condition) {
			this.condition = condition;
		}

		public boolean isRequired() {
			return required;
		}

	}

	@Override
	public void initialize() {

	}

	@Override
	public void runDone() {

	}

	@Override
	public void pause() {

	}

}