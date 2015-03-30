package com.example.boris.goglobaltask;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by boris on 3/24/15.
 */
public class OtherFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_other, container, false);
        ListView listOther = (ListView) view.findViewById(R.id.otherList);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.other_fragment, android.R.layout.simple_list_item_1);

        listOther.setAdapter(adapter);

        listOther.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch (position){
                    case 0:
                        goToOtherFragment(new FeedbackFragment());
                        break;
                    case 1:
                        showProperties();
                        break;
                }
            }
        });

        return view;
    }

    private void showProperties() {
        String version = "";
        String packageName = "";
        try {
            PackageManager manager = getActivity().getPackageManager();
            PackageInfo info = manager.getPackageInfo(
                    getActivity().getPackageName(), 0);
            version = info.versionName;
            packageName = info.packageName;
        } catch (Exception e) {}

        Toast.makeText(getActivity(), "Package:\n" + packageName + "\nVersion: " + version,
                Toast.LENGTH_LONG).show();
    }
    private void goToOtherFragment(Fragment fragment) {
        FragmentTransaction fTrans;
        fTrans = getFragmentManager().beginTransaction();
        fTrans.replace(R.id.container, fragment);
        fTrans.commit();
    }
}
