package com.example.drive;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveServiceHelper {

    private final Executor nExecutor = Executors.newSingleThreadExecutor();
    private Drive mDriveService;

    public DriveServiceHelper(Drive mDriveService) {
        this.mDriveService = mDriveService;
    }


    public Task<String> createFilePdf(String filePath) {
        return Tasks.call(nExecutor,() -> {

            File fileMetaData = new File();
            fileMetaData.setName("MyPDF");

            java.io.File file = new java.io.File(filePath);
            FileContent mediaContent = new FileContent("application/pdf", file);

            File myFile = null;
            try {
                myFile = mDriveService.files().create(fileMetaData, mediaContent).execute();
            } catch (Exception e) {
            }

            if (myFile == null) {
                throw new IOException("Null result when requesting file creation");
            }

            return myFile.getId();
        });

        }

}
