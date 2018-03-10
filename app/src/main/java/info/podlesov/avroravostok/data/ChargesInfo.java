package info.podlesov.avroravostok.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by voseldop on 13/02/2018.
 */

public class ChargesInfo {
    public static final String [] heading = {"Номер","Наименование услуги", "Тариф","Норматив","Полная стоимость", "Льгота", "Перерасчет", "К оплате"};
    public static class ChargesData {
        private Map<String, String> data = new HashMap<>(38);

        public void putValue(String k, String v) {
             data.put(k,v);
        }

        public String getValue(int i) {
            return data.get(heading[i]);
        }
    }

    private List<ChargesData> entries;

    ChargesInfo(List<ChargesData> entries) {
        this.entries = entries;
    }

    public List<ChargesData> getCharges() {
        return entries;
    }

    public static ChargesInfo fromString(String chargesData) throws JSONException {
        JSONObject data = new JSONObject(chargesData);
        int total = data.getInt("records");
        Map<String, Integer> cntrMap = new HashMap<>(total);
        JSONArray rows = data.getJSONArray("rows");
        List<ChargesData> entries = new ArrayList<>(total);
        for (int i = 0; i< rows.length(); i++) {
            JSONObject row = rows.getJSONObject(i);
            JSONArray cells = row.getJSONArray("cell");
            ChargesData item = new ChargesData();
            for (int j =0; j<heading.length; j++) {
                item.putValue(heading[j], cells.isNull(j) ? "" : cells.getString(j));
            }
            entries.add(item);
        }

        return new ChargesInfo(entries);
    }
}
