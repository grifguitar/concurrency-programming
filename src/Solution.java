import org.jetbrains.annotations.NotNull;

/**
 * В теле класса решения разрешено использовать только финальные переменные типа RegularInt.
 * Нельзя volatile, нельзя другие типы, нельзя блокировки, нельзя лазить в глобальные переменные.
 *
 * @author Khlytin Grigoriy
 */
public class Solution implements MonotonicClock {
    private final RegularInt fst1 = new RegularInt(0);
    private final RegularInt fst2 = new RegularInt(0);
    private final RegularInt fst3 = new RegularInt(0);
    private final RegularInt snd1 = new RegularInt(0);
    private final RegularInt snd2 = new RegularInt(0);
    private final RegularInt snd3 = new RegularInt(0);

    @Override
    public void write(@NotNull Time time) {
        snd1.setValue(time.getD1());
        snd2.setValue(time.getD2());
        snd3.setValue(time.getD3());
        fst3.setValue(snd3.getValue());
        fst2.setValue(snd2.getValue());
        fst1.setValue(snd1.getValue());
    }

    @NotNull
    @Override
    public Time read() {
        int[] a = new int[4];
        a[1] = fst1.getValue();
        a[2] = fst2.getValue();
        a[3] = fst3.getValue();
        int[] b = new int[4];
        b[3] = snd3.getValue();
        b[2] = snd2.getValue();
        b[1] = snd1.getValue();
        if (a[1] == b[1] && a[2] == b[2] && a[3] == b[3]) {
            return new Time(a[1], a[2], a[3]);
        } else {
            int p = 0;
            while (a[p] == b[p]) p++;
            for (int i = p + 1; i <= 3; i++) {
                b[i] = 0;
            }
            return new Time(b[1], b[2], b[3]);
        }
    }
}
