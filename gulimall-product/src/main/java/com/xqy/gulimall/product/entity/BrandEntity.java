package com.xqy.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.xqy.common.valid.AddGroup;
import com.xqy.common.valid.ListValue;
import com.xqy.common.valid.UpdateGroup;
import com.xqy.common.valid.UpdateStatusGroup;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author xieqianyu
 * @email 119596909@qq.com
 * @date 2022-12-05 20:06:32
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@NotNull(message = "修改必须指定id" , groups = {UpdateGroup.class , UpdateStatusGroup.class})
	@Null(message = "新增不能指定id" , groups = {AddGroup.class})
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名不能为空" , groups = { AddGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@NotEmpty(message = "品牌logo不能为空" , groups = { AddGroup.class})
	@URL(message = "品牌logo必须是合法的url地址" , groups = { AddGroup.class , UpdateGroup.class})
	private String logo;
	/**
	 * 介绍
	 */

	@NotBlank(message = "品牌介绍不能为空" , groups = { AddGroup.class})
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(message = "显示状态不能为空" , groups = { AddGroup.class , UpdateGroup.class , UpdateStatusGroup.class})
//	@Range(min = 0, max = 1,)
	@ListValue(values = {0,1} , message="显示状态必须是0或者1，0-不显示；1-显示" ,groups = { AddGroup.class  , UpdateGroup.class, UpdateStatusGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */

	@NotBlank(message = "检索首字母不能为空" , groups = { AddGroup.class})
	@Pattern(regexp = "^[a-zA-Z]$" , message = "检索首字母必须是一个英文字母" , groups = { AddGroup.class , UpdateGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(message = "排序不能为空", groups = { AddGroup.class})
	@Min(value = 0, message = "排序必须大于等于0" , groups = {AddGroup.class , UpdateGroup.class})
	private Integer sort;

}
