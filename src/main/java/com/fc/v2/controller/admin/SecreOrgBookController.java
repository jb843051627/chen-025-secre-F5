package com.fc.v2.controller.admin;

import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TSecreOrgBook;
import com.fc.v2.model.auto.TSecreOrgSyncConflict;
import com.fc.v2.service.ITSecreOrgBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 机构名录底册 Controller
 *
 * 上级名录每天下发一份：/reconcileUpper 逐条对账，冲突以本机关自录为准、点代号报出、缘由落本地备注。
 *
 * @author 市保密办规矩落地
 */
@Api(value = "机构名录底册")
@Controller
@RequestMapping("/SecreOrgBookController")
public class SecreOrgBookController extends BaseController {

    @Autowired
    private ITSecreOrgBookService secreOrgBookService;

    @ApiOperation(value = "名录列表", notes = "同代号以本机关自录那份作准，含已撤出（撤出不抹名录）")
    @GetMapping("/list")
    @RequiresPermissions("secre:secreOrgBook:list")
    @ResponseBody
    public ResultTable list() {
        List<TSecreOrgBook> list = secreOrgBookService.listAll();
        return dataTable(list);
    }

    @Log(title = "机构名录本机关自录", action = "saveLocal")
    @ApiOperation(value = "本机关自录", notes = "本机关自录一条名录")
    @PostMapping("/saveLocal")
    @RequiresPermissions("secre:secreOrgBook:edit")
    @ResponseBody
    public AjaxResult saveLocal(TSecreOrgBook book) {
        return toAjax(secreOrgBookService.saveLocal(book));
    }

    /**
     * 每日上级名录下发对账：报文为上级那份清单（逐条）。
     * 返回 data 为本次逐条点到代号的冲突清单；无冲突返回空清单——冲突绝不悄悄盖掉。
     */
    @Log(title = "上级名录下发对账", action = "reconcileUpper")
    @ApiOperation(value = "上级名录下发对账", notes = "逐条对账，冲突以本地为准并点名")
    @PostMapping("/reconcileUpper")
    @RequiresPermissions("secre:secreOrgBook:sync")
    @ResponseBody
    public AjaxResult reconcileUpper(@RequestBody List<TSecreOrgBook> upperRows) {
        List<TSecreOrgSyncConflict> conflicts = secreOrgBookService.reconcileFromUpper(upperRows);
        String msg = conflicts.isEmpty()
                ? "上级名录对账完成，同代号两份一致，无冲突"
                : "上级名录对账完成，逐条点名冲突 " + conflicts.size() + " 条（以本机关自录为准，已落本地名录备注）";
        return success(200, msg, conflicts);
    }

    @ApiOperation(value = "冲突台账", notes = "逐条点代号列出未销冲突")
    @GetMapping("/conflicts")
    @RequiresPermissions("secre:secreOrgBook:list")
    @ResponseBody
    public ResultTable conflicts() {
        return dataTable(secreOrgBookService.listConflicts());
    }
}
