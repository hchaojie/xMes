package com.pig4cloud.pig.mes;

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
// 主类位于 com.pig4cloud.pig.mes 包：自动覆盖 quality 与 material 两个子包的组件与 Mapper 扫描
@SpringBootApplication
public class XmesQualityApplication {

	public static void main(String[] args) {
		SpringApplication.run(XmesQualityApplication.class, args);
	}

}
