package com.fc.v2.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fc.v2.model.auto.TSecreCarrier;

import java.util.List;

/**
 * 涉密载体册面 Service接口
 *
 * 规矩口径（市保密办今年定）：
 * - 还差几件 = 应交 - 已收，只在 CarrierLedger 一处当场轧出，报文带多少都不作数；
 * - 立不立得住（应交/已收/名录代号）按栏位点名打回；
 * - 行只按名录代号文本挂名录，不拿名录主键挂；
 * - 名录撤出只挡新行，不抹旧账；
 * - 见哪几行只认 CarrierScopeService 的视野。
 *
 * @author fuce
 * @date 2026-09-12
 */
public interface ITSecreCarrierService {

    /** 按主键查询（过知悉范围；页面所见的差额当场重轧） */
    TSecreCarrier selectTSecreCarrierById(Long id);

    /** 按条件查询列表（分页由调用方统一处理；自动套知悉范围，差额逐行重轧） */
    List<TSecreCarrier> selectTSecreCarrierList(Wrapper<TSecreCarrier> queryWrapper);

    /**
     * 按册面查询条件 + 季度（形如 2026Q3；null 当季，"all" 不按季）列本屏的行。
     * 报件码精确查时不按季，保证库里入过的每一行在本屏都寻得着。
     */
    List<TSecreCarrier> listForScreen(TSecreCarrier query, String quarter);

    /** 新增（立账校验不过按栏位抛 SecreLedgerException） */
    int insertTSecreCarrier(TSecreCarrier record);

    /** 修改（已收件数改口、换挂靠名录都在落库前重轧差额） */
    int updateTSecreCarrier(TSecreCarrier record);

    /** 批量删除（逐个过知悉范围） */
    int deleteTSecreCarrierByIds(String ids);

    /** 按主键删除（过知悉范围） */
    int deleteTSecreCarrierById(Long id);
}
