package pw.phylame.scj;

import pw.phylame.gaf.Settings;
import pw.phylame.jem.core.Jem;

import java.util.Date;
import java.util.Locale;

/**
 * Configurations for SCJ.
 */
public class AppConfig extends Settings {

    public AppConfig() {
        super();
    }

    @Override
    public void reset() {
        clear();

        setComment("Configurations for PW SCJ\nCreate: "+new Date());

        setAppLocale(Locale.getDefault());
        setDefaultFormat(getDefaultFormat());
    }

    public Locale getAppLocale() {
        return getLocal("app.locale", Locale.getDefault());
    }

    public void setAppLocale(Locale locale) {
        setLocale("app.locale", locale, "Locale settings for SCJ");
    }

    public String getDefaultFormat() {
        return getString("jem.defaultFormat", Jem.PMAB_FORMAT);
    }

    public void setDefaultFormat(String format) {
        setString("jem.defaultFormat", format, "Default format using by Jem");
    }
}
