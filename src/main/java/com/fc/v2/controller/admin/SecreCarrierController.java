package com.fc.v2.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TSecreCarrier;
import com.fc.v2.service.ITSecreCarrierService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * 涉密载体册面 Controller
 *
 * 入册、改口、读数全走 Service 内唯一判路（CarrierLedger / CarrierScopeService），
 * 本层只收报文、回报文，不另算差额、不另判视野。
 *
 * @author fuce
 * @date 2026-09-12
 */
@Api(value = "涉密载体册面")
@Controller
@RequestMapping("/SecreCarrierController")
public class SecreCarrierController extends BaseController {

    private final String prefix = "admin/secreCarrier";

    @Autowired
    private ITSecreCarrierService secreCarrierService;

    @ApiOperation(value = "分页跳转", notes = "分页跳转")
    @GetMapping("/view")
    @RequiresPermissions("secre:secreCarrier:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    @Log(title = "涉密载体册面集合查询", action = "list")
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/list")
    @RequiresPermissions("secre:secreCarrier:list")
    @ResponseBody
    public ResultTable list(TSecreCarrier record) {
        startPage();
        com.github.pagehelper.PageInfo<TSecreCarrier> page =
                new com.github.pagehelper.PageInfo<TSecreCarrier>(
                        secreCarrierService.selectTSecreCarrierList(new QueryWrapper<TSecreCarrier>()));
        return pageTable(page.getList(), page.getTotal());
    }

    /**
     * 按季对册那一屏：本季（或指定 yyyyQn）本屏见得到的行；件码精确查不按季，保证逐行寻得着。
     */
    @Log(title = "涉密载体册面对册屏", action = "screen")
    @ApiOperation(value = "对册屏查询", notes = "按季+知悉范围列行")
    @GetMapping("/screen")
    @RequiresPermissions("secre:secreCarrier:list")
    @ResponseBody
    public ResultTable screen(TSecreCarrier record,
                              @RequestParam(value = "quarter", required = false) String quarter) {
        startPage();
        com.github.pagehelper.PageInfo<TSecreCarrier> page =
                new com.github.pagehelper.PageInfo<TSecreCarrier>(
                        secreCarrierService.listForScreen(record, quarter));
        return pageTable(page.getList(), page.getTotal());
    }

    /** 逐行回查：库里入过的每一行本屏都寻得着，别家的行点不开 */
    @ApiOperation(value = "按主键回查", notes = "按主键回查")
    @GetMapping("/get/{id}")
    @RequiresPermissions("secre:secreCarrier:view")
    @ResponseBody
    public AjaxResult get(@PathVariable("id") Long id) {
        TSecreCarrier row = secreCarrierService.selectTSecreCarrierById(id);
        return row == null ? error("这一行在本屏册面上寻不着") : retobject(200, row);
    }

    @ApiOperation(value = "新增跳转", notes = "新增跳转")
    @GetMapping("/add")
    public String add(ModelMap model) {
        return prefix + "/add";
    }

    @ApiOperation(value = "修改跳转", notes = "修改跳转")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap model) {
        model.put("carrier", secreCarrierService.selectTSecreCarrierById(id));
        return prefix + "/edit";
    }

    @Log(title = "涉密载体册面新增", action = "add")
    @ApiOperation(value = "新增", notes = "新增")
    @PostMapping("/add")
    @RequiresPermissions("secre:secreCarrier:add")
    @ResponseBody
    public AjaxResult add(TSecreCarrier record) {
        return toAjax(secreCarrierService.insertTSecreCarrier(record));
    }

    /**
     * 同义的另一种入册姿势：报文形状与 /add 完全同义（同一批字段、同一处判路、同一处轧账），
     * 只换了个入口名接住；原 /add 收什么回什么维持原状。
     */
    @Log(title = "涉密载体册面入册", action = "register")
    @ApiOperation(value = "入册（/add 同义入口）", notes = "入册")
    @PostMapping("/register")
    @RequiresPermissions("secre:secreCarrier:add")
    @ResponseBody
    public AjaxResult register(TSecreCarrier record) {
        return toAjax(secreCarrierService.insertTSecreCarrier(record));
    }

    @Log(title = "涉密载体册面修改", action = "edit")
    @ApiOperation(value = "修改保存", notes = "修改保存")
    @PostMapping("/edit")
    @RequiresPermissions("secre:secreCarrier:edit")
    @ResponseBody
    public AjaxResult editSave(TSecreCarrier record) {
        return toAjax(secreCarrierService.updateTSecreCarrier(record));
    }

    @Log(title = "涉密载体册面删除", action = "remove")
    @ApiOperation(value = "删除", notes = "删除")
    @DeleteMapping("/remove")
    @RequiresPermissions("secre:secreCarrier:remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(secreCarrierService.deleteTSecreCarrierByIds(ids));
    }
}
