package com.example.app5;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.gsls.gt.GT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@GT.Annotations.GT_AnnotationFloatingWindow(R.layout.demo_floating_window)
public class DemoFloatingWindow extends GT.GT_FloatingWindow.AnnotationFloatingWindow {

    private TextView cpuInfoTextView;
    private TextView memoryInfoTextView;
    private Handler handler;
    private static final int REFRESH_INTERVAL = 1000; // 刷新频率，单位毫秒
    private static final String CPU_FILENAME = "cpu_info.txt";
    private static final String MEMORY_FILENAME = "memory_info.txt";

    @Override
    protected void initView(View view) {
        super.initView(view);
        setDrag(true); // 设置可拖动

        cpuInfoTextView = view.findViewById(R.id.cpuInfoTextView);
        memoryInfoTextView = view.findViewById(R.id.memoryInfoTextView);

        handler = new Handler(Looper.getMainLooper());
        startMonitoring();
    }

    private void startMonitoring() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 获取并记录 CPU 信息
                String cpuInfo = getAllCPUFrequencies();
                saveToFile(CPU_FILENAME, cpuInfo);
                cpuInfoTextView.setText("所有核心的 CPU 频率信息:\n" + cpuInfo);

                // 获取并记录内存信息
                String memoryInfo = getMemoryInfo();
                saveToFile(MEMORY_FILENAME, memoryInfo);
                memoryInfoTextView.setText("内存信息:\n" + memoryInfo);

                // 继续定时查询
                handler.postDelayed(this, REFRESH_INTERVAL);
            }
        }, REFRESH_INTERVAL);
    }

    private String getAllCPUFrequencies() {
        StringBuilder allCoresInfo = new StringBuilder();

        for (int cpuNumber = 0; cpuNumber <= 5; cpuNumber++) {
            String cpuFrequencyPath = String.format("/sys/devices/system/cpu/cpu%d/cpufreq/scaling_cur_freq", cpuNumber);
            long cpuFrequency = readCpuFrequency(cpuFrequencyPath);
            allCoresInfo.append(getCurrentTime()).append(" - ");
            allCoresInfo.append("CPU").append(cpuNumber).append(" 频率: ").append(cpuFrequency).append(" MHz\n");
        }

        return allCoresInfo.toString();
    }

    private long readCpuFrequency(String cpuFrequencyPath) {
        try (BufferedReader br = new BufferedReader(new FileReader(cpuFrequencyPath))) {
            String line;
            if ((line = br.readLine()) != null) {
                // 将 CPU 频率转换成 MHz
                long freqInKHz = Long.parseLong(line.trim());
                return freqInKHz / 1000;
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String getMemoryInfo() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);

            long totalMemory = memoryInfo.totalMem / (1024 * 1024); // 转换为 MB
            long freeMemory = memoryInfo.availMem / (1024 * 1024); // 转换为 MB

            return getCurrentTime() + " - " +
                    "总内存: " + totalMemory + " MB\n可用内存: " + freeMemory + " MB";
        } else {
            return "无法获取内存信息";
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void saveToFile(String filename, String data) {
        try {
            File file = new File(getFilesDir(), filename);
            FileWriter fileWriter = new FileWriter(file, true); // 设置为 true，以便将数据追加到文件末尾
            fileWriter.append(data).append("\n"); // 使用 append 方法追加数据
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GT.Annotations.GT_Click({R.id.btn_ok, R.id.tv_back, R.id.btn_cancel})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_ok:
                GT.toast("单击了ok");
                break;
            case R.id.tv_back:
            case R.id.btn_cancel:
                finish(); // 关闭当前悬浮窗
                break;
        }
    }
}