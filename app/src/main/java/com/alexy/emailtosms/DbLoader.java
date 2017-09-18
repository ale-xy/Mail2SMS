package com.alexy.emailtosms;

import android.text.TextUtils;
import android.util.Log;

import com.alexy.emailtosms.data.UserConfigItem;
import com.orm.SugarRecord;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseBool;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.LMinMax;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.StrRegEx;
import org.supercsv.cellprocessor.constraint.Unique;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CsvContext;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alexeykrichun on 12/09/2017.
 */

public class DbLoader {

    private static class OkParser implements StringCellProcessor {

        @Override
        public Boolean execute(Object value, CsvContext context) {
            if (value instanceof String) {
                if (TextUtils.equals("ok", ((String) value).toLowerCase())) {
                    return true;
                }
            }

            return false;
        }
    }

    private static CellProcessor[] getProcessors() {

        final CellProcessor[] processors = new CellProcessor[] {
                new Unique(), // id
                new Optional(), // name
                new Optional(), // num1
                new Optional(), // num2
                new Optional(), // num3
                new OkParser(), // status
                new Optional(), // misc1
                new Optional(), // misc2
                new Optional(), // misc3
        };

        return processors;
    }

    public static synchronized Map<String, UserConfigItem> readDb(String filename) throws Exception {
        ICsvBeanReader beanReader = null;

        try {
            beanReader = new CsvBeanReader(new FileReader(filename), CsvPreference.STANDARD_PREFERENCE);

            // the header elements are used to map the values to the bean (names must match)
            final String[] header = beanReader.getHeader(true);
            final CellProcessor[] processors = getProcessors();

            UserConfigItem userConfigItem;
            Map<String, UserConfigItem> items = new HashMap();

            while((userConfigItem = beanReader.read(UserConfigItem.class, UserConfigItem.HEADERS, processors)) != null ) {
                items.put(userConfigItem.getUserId(), userConfigItem);

                if (userConfigItem != null) {
                    SugarRecord.update(userConfigItem);
                }

                Log.d("DbLoader", String.format("lineNo=%s, rowNo=%s, item=%s", beanReader.getLineNumber(),
                        beanReader.getRowNumber(), userConfigItem));
            }

            Thread.sleep(500);

            return items;
        } finally {
            if(beanReader != null) {
                beanReader.close();
            }
        }

    }
}
