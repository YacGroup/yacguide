package com.example.paetz.yacguide.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paetz.yacguide.R;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

public class FileChooser {

    private final String _PARENT_DIR = "..";

    private final Context _context;
    private ListView _list;
    private Dialog _dialog;
    private File _currentPath;

    public interface FileSelectedListener {
        void fileSelected(File file);
    }
    private FileSelectedListener _fileListener;

    public FileChooser setFileListener(FileSelectedListener fileListener) {
        _fileListener = fileListener;
        return this;
    }

    public FileChooser(Context context, String defaultFileName) {
        _context = context;
        _currentPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        _dialog = new Dialog(context);
        _dialog.setContentView(R.layout.choose_file_dialog);

        final EditText fileNameEditText = (EditText) _dialog.findViewById(R.id.fileNameEditText);
        _list = (ListView) _dialog.findViewById(R.id.filesListView);
        _list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                final File chosenFile = _getChosenFile((String) _list.getItemAtPosition(which));
                if (chosenFile.isDirectory()) {
                    _displayContent(chosenFile);
                } else {
                    fileNameEditText.setText(chosenFile.getName());
                }
            }
        });
        _dialog.findViewById(R.id.okButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = fileNameEditText.getText().toString();
                if (fileName.isEmpty()) {
                    Toast.makeText(_dialog.getContext(), "Keine Datei ausgewählt", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!fileName.toLowerCase().endsWith(".json")) {
                    fileName += ".json";
                    Toast.makeText(_dialog.getContext(), "Dateisuffix \".json\" automatisch angefügt", Toast.LENGTH_SHORT).show();
                }
                if (_fileListener != null) {
                    _fileListener.fileSelected(new File(_currentPath, fileName));
                }
                _dialog.dismiss();
            }
        });
        _dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _dialog.dismiss();
            }
        });
        _dialog.findViewById(R.id.chooseFileButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _displayContent(_currentPath);
            }
        });

        ((TextView) _dialog.findViewById(R.id.currentPathTextView)).setText("Pfad: " + _currentPath.getAbsolutePath());
        fileNameEditText.setText(defaultFileName);
    }

    public void showDialog() {
        _dialog.show();
    }

    private void _displayContent(File path) {
        _currentPath = path;
        if (!_currentPath.exists()) {
            _currentPath.mkdirs();
        }
        File[] dirs = _currentPath.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (file.canRead() && file.isDirectory());
            }
        });
        File[] files = _currentPath.listFiles(new FileFilter() {
            @Override public boolean accept(File file) {
                return (file.canRead() && !file.isDirectory() && file.getName().toLowerCase().endsWith(".json"));
            }
        });

        int i = 0;
        String[] fileList;
        if (_currentPath.getParentFile() == null || _currentPath.getParentFile().listFiles() == null) {
            fileList = new String[dirs.length + files.length];
        } else {
            fileList = new String[dirs.length + files.length + 1];
            fileList[i++] = _PARENT_DIR;
        }
        Arrays.sort(dirs);
        Arrays.sort(files);
        for (final File dir : dirs ) {
            fileList[i++] = dir.getName();
        }
        for (final File file : files ) {
            fileList[i++] = file.getName();
        }

        ((TextView) _dialog.findViewById(R.id.currentPathTextView)).setText("Pfad: " + _currentPath.getAbsolutePath());
        _list.setAdapter(new ArrayAdapter(_context,
                android.R.layout.simple_list_item_1, fileList) {
            @Override public View getView(int pos, View view, ViewGroup parent) {
                view = super.getView(pos, view, parent);
                ((TextView) view).setSingleLine(true);
                return view;
            }
        });
    }

    private File _getChosenFile(String fileName) {
        if (fileName.equals(_PARENT_DIR)) {
            return _currentPath.getParentFile();
        } else {
            return new File(_currentPath, fileName);
        }
    }
}
