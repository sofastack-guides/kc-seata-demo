package io.sofastack.stockmng.impl;

import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.alipay.sofa.runtime.api.annotation.SofaReferenceBinding;
import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;
import io.seata.spring.annotation.GlobalTransactional;
import io.sofastack.balance.manage.facade.BalanceMngFacade;
import io.sofastack.stockmng.facade.StockMngFacade;
import io.sofastack.stockmng.mapper.StockMngMapper;
import io.sofastack.stockmng.model.ProductInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author yuanyuan
 * @since 2019/6/10
 */
@Service
@SofaService(interfaceType = StockMngFacade.class, uniqueId = "${service.unique.id}", bindings = {
        @SofaServiceBinding(bindingType = "bolt") })
public class StockMngImpl implements StockMngFacade {

    @Resource
    private StockMngMapper stockMngMapper;

    @SofaReference(interfaceType = BalanceMngFacade.class, uniqueId = "${service.unique.id}", binding = @SofaReferenceBinding(bindingType = "bolt"))
    private BalanceMngFacade balanceMngFacade;

    @Override
    public List<ProductInfo> query(String userName) {
        Integer stockRecordCount = stockMngMapper.getStockRecordCountForUserName(userName);
        if (stockRecordCount == null) {
            initUser(userName);
        }

        return stockMngMapper.query(userName);
    }

    private void initUser(String userName) {
        BigDecimal[] prices = new BigDecimal[] {new BigDecimal(0.0), new BigDecimal(50.0), new BigDecimal(80.0),
            new BigDecimal(15.0)};
        for (int i = 1; i <= 3; i++) {
            stockMngMapper.insertStockRecord(
                "0000" + i, "测试商品" + i, "<h2>测试商品描述" + i + "</h2>" + ITEM_DESCRIPTION, prices[i], 10000, userName
            );
        }
    }

    /**
     *
     * 购买商品
     */
    @Override
    @GlobalTransactional(timeoutMills = 3000000, name = "kc-book-store-tx")
    public void purchase(String userName, String productCode, int count) {
        BigDecimal productPrice = stockMngMapper.queryProductPrice(productCode, userName);
        if (productPrice == null) {
            throw new RuntimeException("product code does not exist");
        }
        if (count <= 0) {
            throw new RuntimeException("purchase count should not be negative");
        }
        balanceMngFacade.minusBalance(userName, productPrice.multiply(new BigDecimal(count)));
        stockMngMapper.purchase(userName, productCode, count);
        stockMngMapper.minusStockCount(userName, productCode, count);
    }

    private static final String ITEM_DESCRIPTION = "<div>\n"
        + "<h3>The standard Lorem Ipsum passage, used since the 1500s</h3><p>\"Lorem ipsum dolor sit amet, "
        + "consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad"
        + " minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis "
        + "aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. "
        + "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est "
        + "laborum.\"</p><h3>Section 1.10.32 of \"de Finibus Bonorum et Malorum\", written by Cicero in 45 "
        + "BC</h3><p>\"Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque "
        + "laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae "
        + "vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, "
        + "sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, "
        + "qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora"
        + " incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum "
        + "exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem "
        + "vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui "
        + "dolorem eum fugiat quo voluptas nulla pariatur?\"</p>\n"
        + "<h3>1914 translation by H. Rackham</h3>\n"
        + "<p>\"But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain was "
        + "born and I will give you a complete account of the system, and expound the actual teachings of the great "
        + "explorer of the truth, the master-builder of human happiness. No one rejects, dislikes, or avoids pleasure"
        + " itself, because it is pleasure, but because those who do not know how to pursue pleasure rationally "
        + "encounter consequences that are extremely painful. Nor again is there anyone who loves or pursues or "
        + "desires to obtain pain of itself, because it is pain, but because occasionally circumstances occur in "
        + "which toil and pain can procure him some great pleasure. To take a trivial example, which of us ever "
        + "undertakes laborious physical exercise, except to obtain some advantage from it? But who has any right to "
        + "find fault with a man who chooses to enjoy a pleasure that has no annoying consequences, or one who avoids"
        + " a pain that produces no resultant pleasure?\"</p>\n"
        + "<h3>Section 1.10.33 of \"de Finibus Bonorum et Malorum\", written by Cicero in 45 BC</h3>\n"
        + "<p>\"At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum "
        + "deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, "
        + "similique sunt in culpa qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum "
        + "quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio "
        + "cumque nihil impedit quo minus id quod maxime placeat facere possimus, omnis voluptas assumenda est, omnis"
        + " dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe "
        + "eveniet ut et voluptates repudiandae sint et molestiae non recusandae. Itaque earum rerum hic tenetur a "
        + "sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus "
        + "asperiores repellat.\"</p>\n"
        + "<h3>1914 translation by H. Rackham</h3>\n"
        + "<p>\"On the other hand, we denounce with righteous indignation and dislike men who are so beguiled and "
        + "demoralized by the charms of pleasure of the moment, so blinded by desire, that they cannot foresee the "
        + "pain and trouble that are bound to ensue; and equal blame belongs to those who fail in their duty through "
        + "weakness of will, which is the same as saying through shrinking from toil and pain. These cases are "
        + "perfectly simple and easy to distinguish. In a free hour, when our power of choice is untrammelled and "
        + "when nothing prevents our being able to do what we like best, every pleasure is to be welcomed and every "
        + "pain avoided. But in certain circumstances and owing to the claims of duty or the obligations of business "
        + "it will frequently occur that pleasures have to be repudiated and annoyances accepted. The wise man "
        + "therefore always holds in these matters to this principle of selection: he rejects pleasures to secure "
        + "other greater pleasures, or else he endures pains to avoid worse pains.\"</p>\n"
        + "</div>";

}
