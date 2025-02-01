package org.labs.genesis.config.langage.generator.project;

import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.*;
import java.io.File;
import java.util.Scanner;

public class FolderSelectorCombo {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String destinationFolder = selectDestinationFolder(scanner);
        System.out.println("Selected destination folder: " + destinationFolder);
        scanner.close();
    }

    public static String selectDestinationFolder(Scanner scanner) {
        File selectedFolder = null;
        while (selectedFolder == null) {
            System.out.println("Enter the destination folder path");
            System.out.println("1) Manually");
            System.out.println("2) Using the folder selector");
            System.out.print("Choice index: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    selectedFolder = getFolderFromConsole(scanner);
                    break;
                case "2":
                    selectedFolder = getFolderUsingFileChooser();
                    break;
                default:
                    System.out.println("Invalid option. Please try again.\n");
            }
            if (selectedFolder == null) {
                System.out.println("No folder selected. Please try again.\n");
            }
        }
        String folderPath = selectedFolder.getAbsolutePath();
        System.out.println("Selected folder: " + folderPath + "\n");
        return folderPath;
    }

    private static File getFolderFromConsole(Scanner scanner) {
        System.out.print("Please enter the folder path: ");
        String folderPath = scanner.nextLine().trim();
        File folder = new File(folderPath);
        if (folder.isDirectory()) {
            return folder;
        } else {
            System.out.println("The specified path is not a valid folder.\n");
            return null;
        }
    }

    private static File getFolderUsingFileChooser() {
        try {
            UIManager.setLookAndFeel(new FlatMacLightLaf());
        } catch (Exception e) {
            System.out.println("Error loading FlatLaf. Using default Look and Feel.");
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setApproveButtonText("Select");
        chooser.setDialogTitle("Select a Folder");

        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        } else {
            System.out.println("No folder selected.\n");
            return null;
        }
    }
}