package linked_list_set;


import com.devexperts.dxlab.lincheck.LinChecker;
import com.devexperts.dxlab.lincheck.annotations.Operation;
import com.devexperts.dxlab.lincheck.annotations.Param;
import com.devexperts.dxlab.lincheck.annotations.Reset;
import com.devexperts.dxlab.lincheck.paramgen.IntGen;
import com.devexperts.dxlab.lincheck.stress.StressCTest;
import com.devexperts.dxlab.lincheck.verifier.LongExLinearizabilityVerifier;
import org.junit.Test;


@Param(name = "key", gen = IntGen.class, conf = "1:3")
@StressCTest
@StressCTest(iterations = 10, actorsPerThread = {"30:30", "30:30"},
    verifier = LongExLinearizabilityVerifier.class)
public class LinearizabilityTest {
    private Set set;

    @Reset
    public void reset() {
        set = new SetImpl();
    }

    @Operation(params = "key")
    public boolean add(int x) {
        return set.add(x);
    }

    @Operation(params = "key")
    public boolean contains(int x) {
        return set.contains(x);
    }

    @Operation(params = "key")
    public boolean remove(int x) {
        return set.remove(x);
    }

    @Test
    public void test() {
        LinChecker.check(LinearizabilityTest.class);
    }
}