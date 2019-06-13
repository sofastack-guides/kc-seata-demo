package io.sofastack.balance.manage.facade;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

import java.math.BigDecimal;

/**
 * @author yuanyuan
 * @since 2019/6/10
 */
public interface BalanceMngFacade {

    /**
     *
     * 添加一条用户记录
     *
     * @param userName 用户名
     */
    void createUser(String userName);

    /**
     * 返回用户余额
     *
     * @param userName 用户名
     * @return
     */
    BigDecimal queryBalance(String userName);

    /**
     * 减少用户余额
     *
     * @param userName 用户名
     * @param amount 减少数目
     */
    void minusBalance(String userName, BigDecimal amount);

    @TwoPhaseBusinessAction(name = "minusBalancePrepare", commitMethod = "minusBalanceCommit", rollbackMethod = "minusBalanceRollback")
    boolean minusBalancePrepare(BusinessActionContext context,
                                @BusinessActionContextParameter(paramName = "userName") String userName,
                                @BusinessActionContextParameter(paramName = "amount") BigDecimal amount);

    boolean minusBalanceCommit(BusinessActionContext context);

    boolean minusBalanceRollback(BusinessActionContext context);
}
