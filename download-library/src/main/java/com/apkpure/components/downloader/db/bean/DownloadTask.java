package com.apkpure.components.downloader.db.bean;

import com.apkpure.components.downloader.db.other.TaskStatus;
import com.apkpure.components.downloader.db.other.TaskStatusConverter;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Transient;

import java.util.Date;

/**
 * 下载任务
 *
 * @author xiongke
 * @date 2018/11/5
 */
@Entity(nameInDb = "downloadTask")
public class DownloadTask {
    @Id
    @NotNull
    @Property(nameInDb = "_download_url")
    @Index(unique = true)
    private String downloadUrl;

    @Property(nameInDb = "_short_name")
    private String shortName;

    @Property(nameInDb = "_absolute_path")
    private String absolutePath;

    @Property(nameInDb = "_param_date")
    private String paramData;

    @Convert(converter = TaskStatusConverter.class, columnType = Integer.class)
    @Property(nameInDb = "_task_status_type")
    private TaskStatus taskStatus = TaskStatus.Waiting;

    @Property(nameInDb = "_date")
    private Date date = new Date();

    @Property(nameInDb = "_current_offset")
    private long currentOffset;

    @Property(nameInDb = "_total_length")
    private long totalLength;

    @Property(nameInDb = "_flag")
    private int flag = -1;

    @Property(nameInDb = "_desc")
    private String desc;

    @Transient
    private String taskSpeed;

    @Transient
    private String downloadPercent;

    @Generated(hash = 1089079576)
    public DownloadTask(@NotNull String downloadUrl, String shortName,
            String absolutePath, String paramData, TaskStatus taskStatus, Date date,
            long currentOffset, long totalLength, int flag, String desc) {
        this.downloadUrl = downloadUrl;
        this.shortName = shortName;
        this.absolutePath = absolutePath;
        this.paramData = paramData;
        this.taskStatus = taskStatus;
        this.date = date;
        this.currentOffset = currentOffset;
        this.totalLength = totalLength;
        this.flag = flag;
        this.desc = desc;
    }

    @Generated(hash = 1999398913)
    public DownloadTask() {
    }

    public String getDownloadUrl() {
        return this.downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getShortName() {
        return this.shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getAbsolutePath() {
        return this.absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getParamData() {
        return this.paramData;
    }

    public void setParamData(String paramData) {
        this.paramData = paramData;
    }

    public TaskStatus getTaskStatus() {
        return this.taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getCurrentOffset() {
        return this.currentOffset;
    }

    public void setCurrentOffset(long currentOffset) {
        this.currentOffset = currentOffset;
    }

    public long getTotalLength() {
        return this.totalLength;
    }

    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }

    public int getFlag() {
        return this.flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTaskSpeed() {
        return taskSpeed;
    }

    public void setTaskSpeed(String taskSpeed) {
        this.taskSpeed = taskSpeed;
    }

    public String getDownloadPercent() {
        return downloadPercent;
    }

    public void setDownloadPercent(String downloadPercent) {
        this.downloadPercent = downloadPercent;
    }
}
