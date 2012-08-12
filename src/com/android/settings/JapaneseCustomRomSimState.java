
package com.android.settings;

import java.lang.reflect.Method;
import java.util.ArrayList;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.TelephonyProperties;

public class JapaneseCustomRomSimState extends FrameLayout implements
        View.OnClickListener, DialogInterface.OnClickListener {

    final PresetOperatorList mOperatorList;

    private JapaneseCustomRomSimState(Context context) {
        super(context);

        // preset operator list
        String operators[] = getContext().getResources().getStringArray(R.array.jcrom_operator_preset);
        mOperatorList = new PresetOperatorList(operators);

        // setup view
        addView(View.inflate(getContext(), R.layout.jcrom_simstate, null));
        setupSimState();
    }

    private void setupSimState() {
        // [SIM emulation] check box
        boolean isEmulation = "true".equals(SystemProperties.get(TelephonyProperties.PROPERTY_SIM_EMULATION));
        CheckBox emulation = ((CheckBox)findViewById(R.id.jcrom_simstate_emulate_check));
        emulation.setChecked(isEmulation);
        emulation.setOnClickListener(this);

        // setup SIM operator
        setupOperatorEditor(R.id.jcrom_simstate_sim_operator,
                getContext().getString(R.string.jcrom_operator_sim),
                SystemProperties.get(TelephonyProperties.EMULATION_ICC_OPERATOR_NUMERIC),
                SystemProperties.get(TelephonyProperties.EMULATION_ICC_OPERATOR_ALPHA),
                SystemProperties.get(TelephonyProperties.EMULATION_ICC_OPERATOR_ISO_COUNTRY));

        // [Roaming] check box
        boolean isRoaming = "true".equals(SystemProperties.get(TelephonyProperties.EMULATION_OPERATOR_ISROAMING));
        CheckBox roaming = ((CheckBox)findViewById(R.id.jcrom_simstate_roaming_check));
        roaming.setChecked(isRoaming);
        roaming.setOnClickListener(this);

        // setup Network operator
        setupOperatorEditor(R.id.jcrom_simstate_network_operator,
                getContext().getString(R.string.jcrom_operator_network),
                SystemProperties.get(TelephonyProperties.EMULATION_OPERATOR_NUMERIC),
                SystemProperties.get(TelephonyProperties.EMULATION_OPERATOR_ALPHA),
                SystemProperties.get(TelephonyProperties.EMULATION_OPERATOR_ISO_COUNTRY));

        onClick(emulation);
    }

    private void setupOperatorEditor(int viewId, String title, String mcc, String name, String country) {
        final View operatorView = findViewById(viewId);
        if (operatorView == null) {
            return;
        }
        setText(operatorView, R.id.jcrom_operator_label, title);

        View button = operatorView.findViewById(R.id.jcrom_operator_select);
        if (button != null) {
            button.setOnClickListener(this);
            button.setTag(operatorView);
        }

        if ("".equals(mcc) && "".equals(name) && "".equals(country)) {
            // use default
            mcc = "44010";
            name = "NTT DOCOMO";
            country = "jp";
        }
        setText(operatorView, R.id.jcrom_operator_mcc_edit, mcc);
        setText(operatorView, R.id.jcrom_operator_name_edit, name);
        setText(operatorView, R.id.jcrom_operator_country_edit, country);

        // mcc edited -> country code update
        View editor = operatorView.findViewById(R.id.jcrom_operator_mcc_edit);
        if (editor != null && editor instanceof TextView) {
            ((TextView)editor).setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    String mcc = ((TextView)v).getText().toString();
                    String country = getCountryName(mcc);
                    if (country.length() > 0) {
                        setText(operatorView, R.id.jcrom_operator_country_edit, country);
                    }
                    return false;
                }
            });
        }
    }

    static private void setText(View parent, int viewId, String text) {
        View view = parent.findViewById(viewId);
        if (view != null && view instanceof TextView) {
            ((TextView)view).setText(text);
        }
    }

    private void enableOperatorEditor(int viewId, boolean enabled) {
        View operatorView = findViewById(viewId);
        if (operatorView == null) {
            return;
        }

        int childIds[] = {
                R.id.jcrom_operator_label,
                R.id.jcrom_operator_select,
                R.id.jcrom_operator_mcc_label,
                R.id.jcrom_operator_mcc_edit,
                R.id.jcrom_operator_country_label,
                R.id.jcrom_operator_country_edit,
                R.id.jcrom_operator_name_label,
                R.id.jcrom_operator_name_edit, };
        for (int childId : childIds) {
            View view = operatorView.findViewById(childId);
            if (view != null) {
                view.setEnabled(enabled);
            }
        }
    }

    private void selectOperator(final View operatorView) {
        String titles[] = mOperatorList.getOperatorList();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setItems(titles, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PresetOperator operator = mOperatorList.get(which);
                setText(operatorView, R.id.jcrom_operator_mcc_edit, operator.mcc);
                setText(operatorView, R.id.jcrom_operator_name_edit, operator.name);
                setText(operatorView, R.id.jcrom_operator_country_edit, operator.country);
            }
        });
        builder.show();
    }

    private void saveSimState() {
        CheckBox emulation = ((CheckBox)findViewById(R.id.jcrom_simstate_emulate_check));
        SystemProperties.set(TelephonyProperties.PROPERTY_SIM_EMULATION,
                             emulation.isChecked() ? "true" : "false");

        View operatorView = findViewById(R.id.jcrom_simstate_sim_operator);
        if (operatorView != null) {
            saveProperty(TelephonyProperties.EMULATION_ICC_OPERATOR_NUMERIC,
                         operatorView, R.id.jcrom_operator_mcc_edit);
            saveProperty(TelephonyProperties.EMULATION_ICC_OPERATOR_ALPHA,
                         operatorView, R.id.jcrom_operator_name_edit);
            saveProperty(TelephonyProperties.EMULATION_ICC_OPERATOR_ISO_COUNTRY,
                         operatorView, R.id.jcrom_operator_country_edit);
        }

        CheckBox roaming = ((CheckBox)findViewById(R.id.jcrom_simstate_roaming_check));
        SystemProperties.set(TelephonyProperties.EMULATION_OPERATOR_ISROAMING,
                             roaming.isChecked() ? "true" : "false");

        operatorView = findViewById(R.id.jcrom_simstate_network_operator);
        if (operatorView != null) {
            saveProperty(TelephonyProperties.EMULATION_OPERATOR_NUMERIC,
                         operatorView, R.id.jcrom_operator_mcc_edit);
            saveProperty(TelephonyProperties.EMULATION_OPERATOR_ALPHA,
                         operatorView, R.id.jcrom_operator_name_edit);
            saveProperty(TelephonyProperties.EMULATION_OPERATOR_ISO_COUNTRY,
                         operatorView, R.id.jcrom_operator_country_edit);
        }
    }

    static private void saveProperty(String name, View parent, int viewId) {
        View view = parent.findViewById(viewId);
        if (view != null && view instanceof TextView) {
            SystemProperties.set(name, ((TextView)view).getText().toString());
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        if (viewId == R.id.jcrom_simstate_emulate_check) {
            boolean enabled = ((CheckBox)v).isChecked();
            enableOperatorEditor(R.id.jcrom_simstate_sim_operator, enabled);

            v = findViewById(R.id.jcrom_simstate_roaming_check);
            if (v != null) {
                v.setEnabled(enabled);
                enabled = enabled && ((CheckBox)v).isChecked();
            }
            enableOperatorEditor(R.id.jcrom_simstate_network_operator, enabled);
        } else

        if (viewId == R.id.jcrom_simstate_roaming_check) {
            boolean enabled = ((CheckBox)v).isChecked();
            enableOperatorEditor(R.id.jcrom_simstate_network_operator, enabled);
        } else

        if (viewId == R.id.jcrom_operator_select) {
            selectOperator((View)v.getTag());
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            saveSimState();
        }
    }

    static public Dialog makeDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String prop = SystemProperties.get(TelephonyProperties.PROPERTY_SIM_STATE);
        if ("ABSENT".equals(prop)) {
            // SIM emulation
            JapaneseCustomRomSimState view = new JapaneseCustomRomSimState(context);
            builder.setView(view);
            builder.setPositiveButton(android.R.string.ok, view);
            builder.setNegativeButton(android.R.string.cancel, view);
        } else {
            // use real SIM
            builder.setMessage(R.string.jcrom_cannot_sim_emulation);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        }
        return builder.create();
    }

    static private String getCountryName(String mcc) {
        if (mcc.length() < 3) {
            return "";
        }

        int mcccode = Integer.parseInt(mcc.substring(0, 3));
        return MccTable.countryCodeForMcc(mcccode);
    }

    static class PresetOperator {
        public final String mcc;
        public final String name;
        public final String country;

        PresetOperator(String field) {
            int delim = field.indexOf(',');
            mcc = field.substring(0, delim);
            name = field.substring(delim + 1);
            country = getCountryName(mcc);
        }

        String getTitle() {
            return String.format("%s [%s]", name, mcc);
        }
    }

    static class PresetOperatorList {
        private final ArrayList<PresetOperator> mOperators = new ArrayList<PresetOperator>();

        PresetOperatorList(String operators[]) {
            for (String field : operators) {
                mOperators.add(new PresetOperator(field));
            }
        }

        String[] getOperatorList() {
            ArrayList<String> operatorList = new ArrayList<String>();
            for (int index = 0; index < mOperators.size(); index++) {
                PresetOperator operator = mOperators.get(index);
                operatorList.add(operator.getTitle());
            }
            return operatorList.toArray(new String[0]);
        }

        PresetOperator get(int index) {
            return (index < mOperators.size()) ? mOperators.get(index) : null;
        }
    }
}
