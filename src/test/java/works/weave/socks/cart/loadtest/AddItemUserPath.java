package works.weave.socks.cart.loadtest;

import com.google.common.collect.ImmutableList;
import com.neotys.neoload.model.repository.*;
import com.neotys.neoload.model.v3.project.server.Server;
import com.neotys.neoload.model.v3.project.userpath.*;
import com.neotys.neoload.model.v3.project.userpath.Container;
import com.neotys.neoload.model.v3.project.userpath.Request;
import com.neotys.testing.framework.BaseNeoLoadDesign;
import com.neotys.testing.framework.BaseNeoLoadUserPath;

import java.util.List;
import java.util.Optional;

import static com.neotys.testing.framework.utils.NeoLoadHelper.variabilize;
import static java.util.Collections.emptyList;


public class AddItemUserPath extends BaseNeoLoadUserPath {
    public AddItemUserPath(BaseNeoLoadDesign design) {
        super(design);
    }

    @Override
    public com.neotys.neoload.model.v3.project.userpath.UserPath createVirtualUser(BaseNeoLoadDesign baseNeoLoadDesign) {
        final Server server = baseNeoLoadDesign.getServerByName("carts_host");

        final String jsonPayload=" {\n" +
                "    \"itemId\":\"03fef6ac-1896-4ce8-bd69-b798f85c6e0b\",\n" +
                "    \"unitPrice\":\"99.99\"\n" +
                " }";
        final ImmutableList<com.neotys.neoload.model.v3.project.userpath.Header> headerList= ImmutableList.of(
                header("Cache-Control","no-cache"),
                header("Content-Type","application/json"),
                header("json","true")
        );
        final Request postRequest = postTextBuilderWithHeaders(server,headerList,"/carts/1/items",emptyList(),jsonPayload,emptyList(), Optional.empty()).build();

        final ThinkTime delay = thinkTime(250);
        final Container actionsContainer = actionsContainerBuilder()
                .addSteps(container("AddItem",Optional.empty(),  postRequest,  delay))
                .build();

        return userPathBuilder("AddItemToCart")
                .actions(actionsContainer)
                .build();
    }
}
