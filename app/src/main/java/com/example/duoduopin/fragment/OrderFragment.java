package com.example.duoduopin.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectChangeListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.example.duoduopin.R;
import com.example.duoduopin.activity.AssistantLocationActivity;
import com.example.duoduopin.activity.OneOrderCaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.duoduopin.activity.LoginActivity.JSON;
import static com.example.duoduopin.activity.MainActivity.idContent;
import static com.example.duoduopin.activity.MainActivity.nicknameContent;
import static com.example.duoduopin.activity.MainActivity.tokenContent;
import static com.example.duoduopin.activity.MainActivity.client;
import static com.example.duoduopin.handler.GeneralMsgHandler.ERROR;
import static com.example.duoduopin.handler.GeneralMsgHandler.SUCCESS;
import static com.example.duoduopin.tool.Constants.createOrderUrl;

public class OrderFragment extends Fragment {
    private String typeString;
    private EditText title, description, address, curPeople, maxPeople, price, tude;
    private TextView time;
    private TimePickerView pvTime;
    private String orderId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_order, null);
        return view;
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (this.getView() != null) {
            this.getView().setVisibility(menuVisible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        title = getActivity().findViewById(R.id.titleInput);
        description = getActivity().findViewById(R.id.descriptionInput);
        address = getActivity().findViewById(R.id.addressInput);
        time = getActivity().findViewById(R.id.timeInput);
        curPeople = getActivity().findViewById(R.id.curNumberInput);
        maxPeople = getActivity().findViewById(R.id.maxNumberInput);
        price = getActivity().findViewById(R.id.priceInput);
        tude = getActivity().findViewById(R.id.tudeInput);

        time = getActivity().findViewById(R.id.timeInput);
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pvTime.show(v);
            }
        });
        initTimePicker();

        Spinner spinner = getActivity().findViewById(R.id.typeInput);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.newOrderItemType, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (selected.equals("拼车")) {
                    typeString = "CAR";
                } else if (selected.equals("拼单")) {
                    typeString = "BILL";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                typeString = "";
            }
        });

        Button location = getActivity().findViewById(R.id.locationButton);
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AssistantLocationActivity.class);
                startActivity(intent);
            }
        });

        Button submitOrder = getActivity().findViewById(R.id.submitOrderButton);
        submitOrder.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(final View v) {
                boolean canPost = true;

                final String titleString = title.getText().toString();
                final String descString = description.getText().toString();
                final String addrString = address.getText().toString();
                final String timeString = time.getText().toString();
                final String curPeopleString = curPeople.getText().toString();
                final String maxPeopleString = maxPeople.getText().toString();
                final String priceString = price.getText().toString();
                String latitudeString = "", longitudeString = "";
                if (!tude.getText().toString().isEmpty()) {
                    String[] tudeString = tude.getText().toString().split(",");
                    longitudeString = tudeString[0];
                    latitudeString = tudeString[1];
                }

                if (titleString.isEmpty()) {
                    Toast.makeText(v.getContext(), "请输入标题", Toast.LENGTH_SHORT).show();
                    canPost = false;
                } else if (descString.isEmpty()) {
                    Toast.makeText(v.getContext(), "请输入描述", Toast.LENGTH_SHORT).show();
                    canPost = false;
                } else if (addrString.isEmpty()) {
                    Toast.makeText(v.getContext(), "请输入地址", Toast.LENGTH_SHORT).show();
                    canPost = false;
                } else if (timeString.isEmpty()) {
                    Toast.makeText(v.getContext(), "请输入时间", Toast.LENGTH_SHORT).show();
                    canPost = false;
                } else if (maxPeopleString.isEmpty()) {
                    Toast.makeText(v.getContext(), "请输入最大人数", Toast.LENGTH_SHORT).show();
                    canPost = false;
                } else if (longitudeString.isEmpty()) {
                    Toast.makeText(v.getContext(), "请输入经度", Toast.LENGTH_SHORT).show();
                    canPost = false;
                } else if (latitudeString.isEmpty()) {
                    Toast.makeText(v.getContext(), "请输入纬度", Toast.LENGTH_SHORT).show();
                    canPost = false;
                }
                if (canPost) {
                    final JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("title", titleString);
                        jsonObject.put("type", typeString);
                        jsonObject.put("description", descString);
                        jsonObject.put("address", addrString);
                        jsonObject.put("time", timeString.replace(' ', 'T') + ".0");
                        jsonObject.put("curPeople", curPeopleString);
                        jsonObject.put("maxPeople", maxPeopleString);
                        jsonObject.put("price", priceString);
                        jsonObject.put("longitude", longitudeString);
                        jsonObject.put("latitude", latitudeString);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("JSONBuild", jsonObject.toString());
                    @SuppressLint("HandlerLeak")
                    final Handler newOrderHandler = new Handler(){
                        @Override
                        public void handleMessage(@NonNull Message msg) {
                            if (msg.what == SUCCESS) {
                                Toast.makeText(getActivity(), "创建成功", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getActivity(), OneOrderCaseActivity.class);
                                intent.putExtra("orderId", orderId);
                                intent.putExtra("userId", idContent);
                                intent.putExtra("nickname", nicknameContent);
                                intent.putExtra("type", typeString);
                                intent.putExtra("price", priceString);
                                intent.putExtra("address", addrString);
                                intent.putExtra("curPeople", curPeopleString);
                                intent.putExtra("maxPeople", maxPeopleString);
                                intent.putExtra("time", timeString);
                                intent.putExtra("description", descString);
                                intent.putExtra("title", titleString);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getActivity(), "创建失败，请稍后再试！", Toast.LENGTH_SHORT).show();
                            }
                        }
                    };
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Message message = new Message();
                                message.what = putRequest(jsonObject.toString());
                                newOrderHandler.sendMessage(message);
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int putRequest(String jsonBody) throws IOException, JSONException {
        final String TAG = "createOrder";
        final String contentFromServer = "content";
        final String orderIdFromServer = "id";

        int ret;

        RequestBody body = RequestBody.create(jsonBody, JSON);

        final Request request = new Request.Builder()
                .url(createOrderUrl)
                .header("token", idContent + "_" + tokenContent)
                .put(body)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.code() == 200) {
            JSONObject responseJson = new JSONObject(Objects.requireNonNull(response.body()).string());
            JSONObject contentJson = new JSONObject(responseJson.getString(contentFromServer));
            orderId = contentJson.optString(orderIdFromServer);
            ret = SUCCESS;
        } else {
            Log.d(TAG, Objects.requireNonNull(response.body()).string());
            Log.d(TAG, response.toString());
            ret = ERROR;
        }
        return ret;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getTime(Date date) {//可根据需要自行截取数据显示
        Log.d("getTime()", "choice date millis: " + date.getTime());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    private void initTimePicker() {//Dialog 模式下，在底部弹出
        pvTime = new TimePickerBuilder(getActivity(), new OnTimeSelectListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onTimeSelect(Date date, View v) {
                Toast.makeText(v.getContext(), getTime(date), Toast.LENGTH_SHORT).show();
                Log.i("pvTime", "onTimeSelect");
                time.setText(getTime(date));
            }
        })
                .setTimeSelectChangeListener(new OnTimeSelectChangeListener() {
                    @Override
                    public void onTimeSelectChanged(Date date) {
                        Log.i("pvTime", "onTimeSelectChanged");
                    }
                })
                .setType(new boolean[]{true, true, true, true, true, true})
                .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
                .setLineSpacingMultiplier(2.0f)
                .build();

        Dialog mDialog = pvTime.getDialog();
        if (mDialog != null) {

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM);

            params.leftMargin = 0;
            params.rightMargin = 0;
            pvTime.getDialogContainerLayout().setLayoutParams(params);

            Window dialogWindow = mDialog.getWindow();
            if (dialogWindow != null) {
                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim);//修改动画样式
                dialogWindow.setGravity(Gravity.BOTTOM);//改成Bottom,底部显示
                dialogWindow.setDimAmount(0.3f);
            }
        }
    }

}
