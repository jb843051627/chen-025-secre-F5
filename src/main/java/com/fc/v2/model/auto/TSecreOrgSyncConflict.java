package com.fc.v2.model.auto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * 名录上下发冲突台账对象 t_secre_org_sync_conflict
 *
 * @author 市保密办规矩落地
 */
@TableName("t_secre_org_sync_conflict")
@ApiModel(value = "TSecreOrgSyncConflict", description = "名录上下发对账冲突台账")
public class TSecreOrgSyncConflict implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 起冲突的名录代号 */
    @TableField("book_no")
    @ApiModelProperty(value = "起冲突的名录代号")
    private String bookNo;

    /** 上级那份的单位全称 */
    @TableField("upper_book_name" )
    @ApiModelProperty(value = "上级那份的单位全称")
    private String upperBookName;

    /** 本机关自录那份的单位全称 */
    @TableField("local_book_name")
    @ApiModelProperty(value = "本机关自录那份的单位全称")
    private String localBookName;

    /** 上级那份名录情形 0在册 1已撤号 */
    @TableField("upper_status")
    @ApiModelProperty(value = "上级那份名录情形")
    private Integer upperStatus;

    /** 本机关那份名录情形 0在册 1已撤出 */
    @TableField("local_status")
    @ApiModelProperty(value = "本机关那份名录情形")
    private Integer localStatus;

    /** 冲突缘由与裁定（以本机关自录为准） */
    @TableField("reason")
    @ApiModelProperty(value = "冲突缘由与裁定")
    private String reason;

    /** 删除标记 0正常 1删除 */
    @TableField("del_flag")
    @ApiModelProperty(value = "删除标记 0正常 1删除")
    private Integer delFlag;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建者")
    private String createBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @TableField(value = "update_by", fill = FieldFill.UPDATE)
    @ApiModelProperty(value = "更新者")
    private String updateBy;

    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @TableField("remark")
    @ApiModelProperty(value = "备注")
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookNo() {
        return bookNo;
    }

    public void setBookNo(String bookNo) {
        this.bookNo = bookNo;
    }

    public String getUpperBookName() {
        return upperBookName;
    }

    public void setUpperBookName(String upperBookName) {
        this.upperBookName = upperBookName;
    }

    public String getLocalBookName() {
        return localBookName;
    }

    public void setLocalBookName(String localBookName) {
        this.localBookName = localBookName;
    }

    public Integer getUpperStatus() {
        return upperStatus;
    }

    public void setUpperStatus(Integer upperStatus) {
        this.upperStatus = upperStatus;
    }

    public Integer getLocalStatus() {
        return localStatus;
    }

    public void setLocalStatus(Integer localStatus) {
        this.localStatus = localStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
