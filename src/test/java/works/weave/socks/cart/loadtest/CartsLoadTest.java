package works.weave.socks.cart.loadtest;

import com.neotys.testing.framework.BaseNeoLoadDesign;
import com.neotys.testing.framework.NeoLoadTest;

import java.io.FileNotFoundException;
import java.util.Optional;

public class CartsLoadTest extends NeoLoadTest {
    @Override
    protected BaseNeoLoadDesign design() {
        try {
            return new TestingDesign(Optional.empty());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected String projectName() {
        return "Carts_NeoLoad";
    }

    @Override
    public void createComplexPopulation() {

    }

    @Override
    public void createComplexScenario() {

    }

    @Override
    public void execute() {

        createSimpleConstantLoadScenario("Cart_Load","AddItemToCart",600,49,10, Optional.empty());
        createSimpleConstantIterationScenario("DynatraceSanityCheck","BasicCheckTesting",5,1,0,Optional.empty());
        createSanityCheckScenario();
    }
}
