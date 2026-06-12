package com.pig4cloud.pig.mes.core.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备事件（FR-INT-20：物联网模块推送的标准事件信封，幂等落库）。
 * <p>
 * 消费链路（状态记录投影 / 计数自动报工 / 过程值质量事件）随物联网模块上线启用，
 * 当前仅接收落库以冻结契约。
 *
 * @author xmes
 */
@Data
@TableName("mes_int_device_event")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "设备事件")
public class IntDeviceEvent extends Model<IntDeviceEvent> {

	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	/** 事件唯一标识（幂等键，由采集端生成） */
	@NotBlank(message = "事件标识不能为空")
	@Schema(description = "事件唯一标识（幂等键）")
	private String eventId;

	/** 设备/工位编码 */
	@NotBlank(message = "设备编码不能为空")
	@Schema(description = "设备/工位编码")
	private String equipmentCode;

	/** 事件类型：STATE-状态，COUNT-计数，PROCESS-过程值，ALARM-报警 */
	@NotBlank(message = "事件类型不能为空")
	@Schema(description = "事件类型：STATE/COUNT/PROCESS/ALARM")
	private String eventType;

	/** 字符值（状态码/报警码） */
	@Schema(description = "字符值（状态码/报警码）")
	private String stringValue;

	/** 数值（计数/过程值） */
	@Schema(description = "数值（计数/过程值）")
	private BigDecimal numericValue;

	/** 采集点编码（过程值时） */
	@Schema(description = "采集点编码")
	private String tagCode;

	/** 源时间戳（设备侧发生时间） */
	@Schema(description = "源时间戳")
	private LocalDateTime sourceTime;

	/** 接收时间 */
	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "接收时间")
	private LocalDateTime createTime;

	/** 删除标志 */
	@TableLogic
	@Schema(description = "删除标志")
	private String delFlag;

	/** 租户ID */
	@Schema(description = "租户ID")
	private Long tenantId;

}
