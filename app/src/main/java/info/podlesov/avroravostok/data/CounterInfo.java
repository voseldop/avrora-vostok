package info.podlesov.avroravostok.data;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.podlesov.avroravostok.networking.OfficeHelper;

/**
 * Created by voseldop on 10/10/2017.
 */

public class CounterInfo {
    private List<CounterEntry> counters;

    public boolean isDisabled() {
        int disabled = 0;
        int completed = 0;

        for (CounterInfo.CounterEntry counter : counters) {
            if (!counter.getEnabled()) {
                disabled++;
            } else if (StringUtils.isNotBlank(counter.getValueCurrent())) {
                completed++;
            }
        }
        return disabled == counters.size();
    }

    public boolean isComplete() {
        int disabled = 0;
        int completed = 0;

        for (CounterInfo.CounterEntry counter : counters) {
            if (!counter.getEnabled()) {
                disabled++;
            } else if (StringUtils.isNotBlank(counter.getValueCurrent())) {
                completed++;
            }
        }
        return disabled+completed == counters.size();
    }

    public static class CounterEntry {
        private String id;
        private String name;
        private String valueOld;
        private String valueCurrent;
        private Boolean enabled;
        private String date;

        public CounterEntry(String id, String name, String valueOld, String valueCurrent, String date, Boolean enabled) {
            this.name = name;
            this.valueOld = valueOld;
            this.valueCurrent = valueCurrent;
            this.enabled = enabled;
            this.date = date;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public String getValueOld() {
            return valueOld;
        }

        public String getValueCurrent() {
            return valueCurrent;
        }

        public void setValueCurrent(String valueCurrent) {
            this.valueCurrent = valueCurrent;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public String getDate() {
            return date;
        }

        public String getId() {
            return id;
        }
    }

    public List<CounterEntry> getCounters() {
        return counters;
    }

    public void setCounters(List<CounterEntry> in) {
        counters = in;
    }

    public static CounterInfo get(OfficeHelper helper) throws JSONException, IOException {
        return CounterInfo.fromString(helper.getCounterData());
    }

    public static CounterInfo fromString(String counterData) throws JSONException {
        JSONObject data = new JSONObject(counterData);
        int total = data.getInt("records");
        Map<String, Integer> cntrMap = new HashMap<>(total);
        JSONArray rows = data.getJSONArray("rows");
        List<CounterEntry> entries = new ArrayList<>(total);
        for (int i = 0; i< total; i++) {
            JSONObject row = rows.getJSONObject(i);
            JSONArray cells = row.getJSONArray("cell");
            String id = row.getString("id");
            String name = cells.getString(1);
            String valueOld = cells.isNull(2) ? "" : cells.getString(2);
            String valueCurrent = cells.isNull(3) ? "" : cells.getString(3);
            String timestamp = cells.isNull(4) ? "" : cells.getString(4);
            Boolean enabled = cells.isNull(5) ? false : cells.getBoolean(5);

            entries.add(new CounterEntry(id, name, valueOld, valueCurrent, timestamp, enabled));
        }
        CounterInfo info =new CounterInfo();
        info.setCounters(entries);
        return info;
    }
}
