package controller;

/**
 * Created by zouyi on 2017/10/31.
 */
public class Constant {
    public static final String workFlowConfig ="{\n" +
            "  \"descriptors\": [\n" +
            "    {\n" +
            "      \"actionName\": \"dw_log_1\",\n" +
            "      \"actionType\": \"NORMORL\",\n" +
            "      \"clazz\": \"com.banggood.xwork.action.impl.ShellAction\",\n" +
            "      \"configs\": {\n" +
            "        \"parameters\": {\n" +
            "          \"xwork.shell.env\": {\n" +
            "            \"content\": {\n" +
            "              \"env\": \"/opt/modules/dw_log_1\"\n" +
            "            },\n" +
            "            \"required\": true,\n" +
            "            \"setContent\": true,\n" +
            "            \"type\": \"MAP\"\n" +
            "          },\n" +
            "          \"xwork.shell.command\": {\n" +
            "            \"content\": \"/bin/bash -c \\\"for((i=1;i<=10;i++));do echo $env ;sleep 1;done\\\"\",\n" +
            "            \"required\": true,\n" +
            "            \"setContent\": true,\n" +
            "            \"type\": \"STRING\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"actionName\": \"dw_log_2\",\n" +
            "      \"actionType\": \"NORMORL\",\n" +
            "      \"clazz\": \"com.banggood.xwork.action.impl.ShellAction\",\n" +
            "      \"configs\": {\n" +
            "        \"parameters\": {\n" +
            "          \"xwork.shell.env\": {\n" +
            "            \"content\": {\n" +
            "              \"env\": \"/opt/modules/dw_log_2\"\n" +
            "            },\n" +
            "            \"required\": true,\n" +
            "            \"setContent\": true,\n" +
            "            \"type\": \"MAP\"\n" +
            "          },\n" +
            "          \"xwork.shell.command\": {\n" +
            "            \"content\": \"/bin/bash -c \\\"for((i=1;i<=10;i++));do echo $env ;sleep 1;done\\\"\",\n" +
            "            \"required\": true,\n" +
            "            \"setContent\": true,\n" +
            "            \"type\": \"STRING\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"actionName\": \"dw_log_3\",\n" +
            "      \"actionType\": \"NORMORL\",\n" +
            "      \"clazz\": \"com.banggood.xwork.action.impl.ShellAction\",\n" +
            "      \"configs\": {\n" +
            "        \"parameters\": {\n" +
            "          \"xwork.shell.env\": {\n" +
            "            \"content\": {\n" +
            "              \"env\": \"/opt/modules/dw_log_3\"\n" +
            "            },\n" +
            "            \"required\": true,\n" +
            "            \"setContent\": true,\n" +
            "            \"type\": \"MAP\"\n" +
            "          },\n" +
            "          \"xwork.shell.command\": {\n" +
            "            \"content\": \"/bin/bash -c \\\"for((i=1;i<=10;i++));do echo $env ;sleep 1;done\\\"\",\n" +
            "            \"required\": true,\n" +
            "            \"setContent\": true,\n" +
            "            \"type\": \"STRING\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"actionName\": \"dw_log_4\",\n" +
            "      \"actionType\": \"NORMORL\",\n" +
            "      \"clazz\": \"com.banggood.xwork.action.impl.ShellAction\",\n" +
            "      \"configs\": {\n" +
            "        \"parameters\": {\n" +
            "          \"xwork.shell.env\": {\n" +
            "            \"content\": {\n" +
            "              \"env\": \"/opt/modules/dw_log_4\"\n" +
            "            },\n" +
            "            \"required\": true,\n" +
            "            \"setContent\": true,\n" +
            "            \"type\": \"MAP\"\n" +
            "          },\n" +
            "          \"xwork.shell.command\": {\n" +
            "            \"content\": \"/bin/bash -c \\\"for((i=1;i<=10;i++));do echo $env ;sleep 1;done\\\"\",\n" +
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
            "        \"content\": \"1111\",\n" +
            "        \"required\": true,\n" +
            "        \"setContent\": true,\n" +
            "        \"type\": \"STRING\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"flowName\": \"dw_log_shell\",\n" +
            "  \"relations\": {\n" +
            "    \"dw_log_1\": {\n" +
            "      \"actionName\": \"dw_log_1\",\n" +
            "      \"children\": [\n" +
            "        \"dw_log_2\"\n" +
            "      ],\n" +
            "      \"fathers\": []\n" +
            "    },\n" +
            "    \"dw_log_2\": {\n" +
            "      \"actionName\": \"dw_log_2\",\n" +
            "      \"children\": [\n" +
            "        \"dw_log_3\"\n" +
            "      ],\n" +
            "      \"fathers\": [\n" +
            "        \"dw_log_1\"\n" +
            "      ]\n" +
            "    },\n" +
            "    \"dw_log_3\": {\n" +
            "      \"actionName\": \"dw_log_3\",\n" +
            "      \"children\": [\n" +
            "        \"dw_log_4\"\n" +
            "      ],\n" +
            "      \"fathers\": [\n" +
            "        \"dw_log_2\"\n" +
            "      ]\n" +
            "    },\n" +
            "    \"dw_log_4\": {\n" +
            "      \"actionName\": \"dw_log_4\",\n" +
            "      \"children\": [\n" +
            "        \"END\"\n" +
            "      ],\n" +
            "      \"fathers\": [\n" +
            "        \"dw_log_3\"\n" +
            "      ]\n" +
            "    },\n" +
            "    \"START\": {\n" +
            "      \"children\": [\n" +
            "        \"dw_log_1\"\n" +
            "      ],\n" +
            "      \"fathers\": []\n" +
            "    },\n" +
            "    \"END\": {\n" +
            "      \"children\": [],\n" +
            "      \"fathers\": [\n" +
            "        \"dw_log_4\"\n" +
            "      ]\n" +
            "    }\n" +
            "  }\n" +
            "}";
}
