package demo;

/**
 * Created by zouyi on 2017/9/19.
 */
public class WorkFlowConfigJson {
    public static  final String workFlowConfig="{\n" +
            "  \"descriptors\": [\n" +
            "    {\n" +
            "      \"actionName\": \"action_1\",\n" +
            "      \"actionType\": \"NORMORL\",\n" +
            "      \"clazz\": \"com.banggood.xwork.action.impl.DemoAction\",\n" +
            "      \"configs\": {\n" +
            "        \"parameters\": {\n" +
            "          \"sleep\": {\n" +
            "            \"content\": \"300\",\n" +
            "            \"required\": true,\n" +
            "            \"setContent\": true,\n" +
            "            \"type\": \"STRING\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"actionName\": \"action_2\",\n" +
            "      \"actionType\": \"NORMORL\",\n" +
            "      \"clazz\": \"com.banggood.xwork.action.impl.ShellAction\",\n" +
            "      \"configs\": {\n" +
            "        \"parameters\": {\n" +
            "          \"xwork.shell.env\": {\n" +
            "            \"content\": {\n" +
            "              \"path\": \"/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/bin:/bin:/Users/zouyi/Library/Maven/apache-maven-3.5.0/bin:/Users/zouyi/Library/module/mysql-5.7.13-osx10.11-x86_64/bin:/Users/zouyi/Downloads/gradle-3.5.1/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin\"\n" +
            "            },\n" +
            "            \"required\": true,\n" +
            "            \"setContent\": true,\n" +
            "            \"type\": \"MAP\"\n" +
            "          },\n" +
            "          \"xwork.shell.command\": {\n" +
            "            \"content\": \"/bin/bash -c \\\"for((i=1;i<=5;i++));do echo \\\\$i;sleep 1;done\\\"\",\n" +
            "            \"required\": true,\n" +
            "            \"setContent\": true,\n" +
            "            \"type\": \"STRING\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"actionName\": \"action_3\",\n" +
            "      \"actionType\": \"NORMORL\",\n" +
            "      \"clazz\": \"com.banggood.xwork.action.impl.ShellAction\",\n" +
            "      \"configs\": {\n" +
            "        \"parameters\": {\n" +
            "          \"xwork.shell.env\": {\n" +
            "            \"content\": {\n" +
            "              \"path\": \"/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/bin:/bin:/Users/zouyi/Library/Maven/apache-maven-3.5.0/bin:/Users/zouyi/Library/module/mysql-5.7.13-osx10.11-x86_64/bin:/Users/zouyi/Downloads/gradle-3.5.1/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin\"\n" +
            "            },\n" +
            "            \"required\": true,\n" +
            "            \"setContent\": true,\n" +
            "            \"type\": \"MAP\"\n" +
            "          },\n" +
            "          \"xwork.shell.command\": {\n" +
            "            \"content\": \"/bin/bash -c \\\"for((i=1;i<=5;i++));do echo \\\\$i;sleep 1;done\\\"\",\n" +
            "            \"required\": true,\n" +
            "            \"setContent\": true,\n" +
            "            \"type\": \"STRING\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"actionName\": \"action_4\",\n" +
            "      \"actionType\": \"NORMORL\",\n" +
            "      \"clazz\": \"com.banggood.xwork.action.impl.DemoAction\",\n" +
            "      \"configs\": {\n" +
            "        \"parameters\": {\n" +
            "          \"sleep\": {\n" +
            "            \"content\": \"300\",\n" +
            "            \"required\": true,\n" +
            "            \"setContent\": true,\n" +
            "            \"type\": \"STRING\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  ],\n" +
            "  \"flowConfig\": {\n" +
            "    \"parameters\": {\n" +
            "      \"runTime\": {\n" +
            "        \"content\": \"11111\",\n" +
            "        \"required\": true,\n" +
            "        \"setContent\": true,\n" +
            "        \"type\": \"STRING\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"flowName\": \"testActions5\",\n" +
            "  \"relations\": {\n" +
            "    \"action_1\": {\n" +
            "      \"actionName\": \"action_1\",\n" +
            "      \"children\": [\n" +
            "        \"action_2\",\n" +
            "        \"action_3\"\n" +
            "      ],\n" +
            "      \"fathers\": []\n" +
            "    },\n" +
            "    \"action_2\": {\n" +
            "      \"actionName\": \"action_2\",\n" +
            "      \"children\": [\n" +
            "        \"action_4\"\n" +
            "      ],\n" +
            "      \"fathers\": [\n" +
            "        \"action_1\"\n" +
            "      ]\n" +
            "    },\n" +
            "    \"action_3\": {\n" +
            "      \"actionName\": \"action_3\",\n" +
            "      \"children\": [\n" +
            "        \"action_4\"\n" +
            "      ],\n" +
            "      \"fathers\": [\n" +
            "        \"action_1\"\n" +
            "      ]\n" +
            "    },\n" +
            "    \"action_4\": {\n" +
            "      \"actionName\": \"action_4\",\n" +
            "      \"children\": [\n" +
            "        \"END\"\n" +
            "      ],\n" +
            "      \"fathers\": [\n" +
            "        \"action_2\",\n" +
            "        \"action_3\"\n" +
            "      ]\n" +
            "    },\n" +
            "    \"START\": {\n" +
            "      \"children\": [\n" +
            "        \"action_1\"\n" +
            "      ],\n" +
            "      \"fathers\": []\n" +
            "    },\n" +
            "    \"END\": {\n" +
            "      \"children\": [],\n" +
            "      \"fathers\": [\n" +
            "        \"action_4\"\n" +
            "      ]\n" +
            "    }\n" +
            "  }\n" +
            "}";
}
