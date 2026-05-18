package com.example.demo.dto;

public class WorkerProfileResponseDto {

    private String workerName;
    private double temperature;
    private int noShowCount;
    private String workerStatus;

    public WorkerProfileResponseDto(
            String workerName,
            double temperature,
            int noShowCount
    ) {
        this.workerName = workerName;
        this.temperature = temperature;
        this.noShowCount = noShowCount;

        // 작업자 상태 계산
        if (noShowCount >= 5) {
            this.workerStatus = "말썽 작업자";
        } else if (noShowCount >= 3) {
            this.workerStatus = "주의 작업자";
        } else {
            this.workerStatus = "정상 작업자";
        }
    }

    public String getWorkerName() { return workerName; }
    public double getTemperature() { return temperature; }
    public int getNoShowCount() { return noShowCount; }
    public String getWorkerStatus() { return workerStatus; }
}