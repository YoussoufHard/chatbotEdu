package ensetproject.chatbotedu.service;

import javafx.stage.Stage;

import java.io.File;

public class FileService {
    private File selectedFile;

    public void selectFile(File file) {
        this.selectedFile = file;
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public void clearSelectedFile() {
        this.selectedFile = null;
    }

    public File chooseFile(Stage stage) {
        //code ici
        return null ;
        
    }

    public File chooseImage(Stage stage) {
        return null ;
    }
}
