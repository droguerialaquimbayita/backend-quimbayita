package org.facturacion.facturacion.config;


import lombok.AllArgsConstructor;
import org.facturacion.facturacion.services.implementation.DatabaseBackupService;
import org.facturacion.facturacion.services.implementation.GithubUploadService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Comparator;
import java.io.File;
import java.nio.file.Path;

@Service
@AllArgsConstructor
public class BackupScheduler {

    private final DatabaseBackupService databaseBackupService;
    private final GithubUploadService githubUploadService;

    @Scheduled(fixedRate = 10800000)  // Cada 3 horas
    public void backupAndUpload() {
        try {
            // Definir el directorio temporal
            String directoryPath = "/tmp/backup_" + System.currentTimeMillis(); // Usa un directorio temporal único

            // Exportar todas las tablas
            databaseBackupService.exportAllTablesToCsv(directoryPath);

            // Subir a GitHub
            String repoName = "droguerialaquimbayita/backup";
            String commitMessage = "Backup de todas las tablas " + LocalDate.now();
            githubUploadService.uploadFilesToRepo(repoName, directoryPath, commitMessage);

            // Opcional: Limpiar el directorio después de la carga
            Files.walk(Paths.get(directoryPath))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);

        } catch (IOException e) {
            e.printStackTrace(); // O utiliza un logger para registrar el error
        }
    }
}
