package com.analytics.analytics_engine;

import com.analytics.analytics_engine.support.MySqlTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(MySqlTestConfiguration.class)
class AnalyticsEngineApplicationTests {

	@Test
	void contextLoads() {
	}

}
