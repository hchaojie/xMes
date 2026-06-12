package com.pig4cloud.pig.mes.execution;

import com.pig4cloud.pig.common.security.annotation.EnablePigResourceServer;
import com.pig4cloud.pig.common.swagger.annotation.EnableOpenApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * xmes-execution 生产执行服务
 * <p>
 * 工单、作业、排程、报工、工位终端
 *
 * @author xmes
 */
@EnableOpenApi("mes-execution")
@EnablePigResourceServer
@EnableDiscoveryClient
@SpringBootApplication
public class XmesExecutionApplication {

	public static void main(String[] args) {
		SpringApplication.run(XmesExecutionApplication.class, args);
	}

}
