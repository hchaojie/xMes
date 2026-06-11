package com.pig4cloud.pig.mes.core;

import com.pig4cloud.pig.common.security.annotation.EnablePigResourceServer;
import com.pig4cloud.pig.common.swagger.annotation.EnableOpenApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * xmes-core 平台与建模服务
 * <p>
 * 车间建模（工厂结构/班次日历）、制造主数据（物料/BOM/工作计划）、ERP 适配
 *
 * @author xmes
 */
@EnableOpenApi("mes")
@EnablePigResourceServer
@EnableDiscoveryClient
@SpringBootApplication
public class XmesCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(XmesCoreApplication.class, args);
	}

}
