package com.pig4cloud.pig.mes.quality;

import com.pig4cloud.pig.common.security.annotation.EnablePigResourceServer;
import com.pig4cloud.pig.common.swagger.annotation.EnableOpenApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * xmes-quality-material 质量与物料服务
 * <p>
 * 检验计划/检验单/不合格品、批次/序列号/追溯谱系
 *
 * @author xmes
 */
@EnableOpenApi("mes-quality")
@EnablePigResourceServer
@EnableDiscoveryClient
@SpringBootApplication
public class XmesQualityApplication {

	public static void main(String[] args) {
		SpringApplication.run(XmesQualityApplication.class, args);
	}

}
