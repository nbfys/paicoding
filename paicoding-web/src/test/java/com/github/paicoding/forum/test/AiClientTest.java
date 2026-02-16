package com.github.paicoding.forum.test; // 请修改为你的实际包名

import com.github.paicoding.forum.service.ai.AiClient;
import com.github.paicoding.forum.web.QuickForumApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
// 1. 指定 classes = 启动类.class (帮助测试找到入口)
@SpringBootTest(classes = QuickForumApplication.class)
// 2. 强制扫描 com.github.paicoding.forum 包下的所有组件
@ComponentScan("com.github.paicoding.forum")
public class AiClientTest {

    @Autowired
    private AiClient aiClient;

    @Test
    public void testGetSummary() {
        String title = "Paicoding 联调测试";
        String content = "这是来自 Paicoding (JDK8) 的调用请求。我们正在测试 RestTemplate 是否能成功连接到运行在 8081 端口的 AI Service。如果成功，我们将获得一段摘要。此测试用于验证微服务间的通信链路是否畅通。";

        System.out.println(">>> [测试开始] 正在调用 AI 服务...");

        String summary = aiClient.getSummary(title, content);

        System.out.println(">>> [测试结果] Summary: " + summary);

        // 断言 (简单验证)
        if (summary != null && !summary.isEmpty()) {
            System.out.println("✅ 测试通过！成功获取到摘要。");
        } else {
            System.err.println("❌ 测试失败！未获取到摘要，请检查 AI 服务日志。");
        }
    }
}