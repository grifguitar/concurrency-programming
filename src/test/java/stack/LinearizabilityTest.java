package stack;


import com.devexperts.dxlab.lincheck.LinChecker;
import com.devexperts.dxlab.lincheck.annotations.Operation;
import com.devexperts.dxlab.lincheck.annotations.Param;
import com.devexperts.dxlab.lincheck.paramgen.IntGen;
import com.devexperts.dxlab.lincheck.strategy.stress.StressCTest;
import org.junit.Test;


@StressCTest
public class LinearizabilityTest {
    private Stack stack = new StackImpl();

    @Operation
    public void push(@Param(gen = IntGen.class, conf = "0:10") int x) {
        stack.push(x);
    }

    @Operation
    public int pop() {
        return stack.pop();
    }

    @Test
    public void test() {
        LinChecker.check(LinearizabilityTest.class);
    }
}