package seedu.address.testutil;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Locale;
import java.util.concurrent.Callable;

/**
 * Runs code with another default locale, as on a computer set to another language.
 */
public class LocaleTestUtil {

    /** Myanmar, which many helpers in Singapore use, and whose digits are not 0 to 9, e.g., 2 is "၂". */
    public static final Locale NON_LATIN_DIGIT_LOCALE = Locale.of("my", "MM");

    /**
     * Returns the result of {@code callable}, called with {@code locale} as the default locale, and then restores the
     * default locales from before the call.
     */
    public static <T> T callWithDefaultLocale(Locale locale, Callable<T> callable) throws Exception {
        requireAllNonNull(locale, callable);
        Locale earlierLocale = Locale.getDefault();
        Locale earlierFormatLocale = Locale.getDefault(Locale.Category.FORMAT);
        Locale earlierDisplayLocale = Locale.getDefault(Locale.Category.DISPLAY);
        Locale.setDefault(locale);

        try {
            return callable.call();
        } finally {
            Locale.setDefault(earlierLocale);
            Locale.setDefault(Locale.Category.FORMAT, earlierFormatLocale);
            Locale.setDefault(Locale.Category.DISPLAY, earlierDisplayLocale);
        }
    }
}
