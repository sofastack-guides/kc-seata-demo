package io.sofastack.balance.manage.impl;

import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.sofastack.balance.manage.facade.BalanceMngFacade;
import io.sofastack.balance.manage.mapper.BalanceMngMapper;
import io.sofastack.balance.manage.model.Balance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @author yuanyuan
 * @since 2019/6/10
 */
@Service
@SofaService(interfaceType = BalanceMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class BalanceMngImpl implements BalanceMngFacade {
    @Resource
    private BalanceMngMapper balanceMngMapper;

    @Override
    public void createUser(String userName) {
        Balance balance = balanceMngMapper.userExists(userName);
        if (balance == null) {
            balanceMngMapper.createUser(userName);
        }
    }

    @Override
    public BigDecimal queryBalance(String userName) {
        Balance balance = balanceMngMapper.queryBalance(userName);
        if (balance == null) {
            throw new RuntimeException("user name does not exist");
        }
        return balance.getBalance();
    }

    @Override
    public void minusBalance(String userName, BigDecimal amount) {
        balanceMngMapper.minusBalance(userName, amount);
    }


    private static final Logger LOGGER = LoggerFactory.getLogger(BalanceMngImpl.class);

    @Override
    public boolean minusBalancePrepare(BusinessActionContext context, String userName, BigDecimal amount) {
        LOGGER.info("minus balance prepare begin ...");
        LOGGER.info("minus balance prepare SQL: update balance_tb set balance = balance - {}, freezed = freezed + {}  where user_name = {}", amount, amount, userName);

        int effect = balanceMngMapper.minusBalancePrepare(userName, amount);
        LOGGER.info("minus balance prepare end");
        return (effect > 0);
    }

    @Override
    public boolean minusBalanceCommit(BusinessActionContext context) {

        //分布式事务ID
        final String xid = context.getXid();

        final String userName = String.valueOf(context.getActionContext("userName"));

        final BigDecimal amount = new BigDecimal(String.valueOf(context.getActionContext("amount")));

        LOGGER.info("minus balance commit begin ... xid: " + xid);
        LOGGER.info("minus balance commit SQL: update balance_tb set freezed = freezed - {}  where user_name = {}", amount, userName);

        int effect = balanceMngMapper.minusBalanceCommit(userName, amount);
        LOGGER.info("minus balance commit end");
        return (effect > 0);
    }

    @Override
    public boolean minusBalanceRollback(BusinessActionContext context) {
        //分布式事务ID
        final String xid = context.getXid();

        final String userName = String.valueOf(context.getActionContext("userName"));

        final BigDecimal amount = new BigDecimal(String.valueOf(context.getActionContext("amount")));

        LOGGER.info("minus balance rollback begin ... xid: " + xid);
        LOGGER.info("minus balance rollback SQL: update balance_tb set balance = balance + {}, freezed = freezed - {}  where user_name = {}", amount, amount, userName);

        int effect = balanceMngMapper.minusBalanceRollback(userName, amount);
        LOGGER.info("minus balance rollback end");
        return (effect > 0);
    }
}
