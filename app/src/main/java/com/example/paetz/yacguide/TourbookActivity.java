package com.example.paetz.yacguide;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paetz.yacguide.database.AppDatabase;
import com.example.paetz.yacguide.database.Ascend;
import com.example.paetz.yacguide.database.Region;
import com.example.paetz.yacguide.database.Rock;
import com.example.paetz.yacguide.database.Route;
import com.example.paetz.yacguide.database.Sector;
import com.example.paetz.yacguide.database.TourbookExporter;
import com.example.paetz.yacguide.utils.FileChooser;
import com.example.paetz.yacguide.utils.FilesystemUtils;
import com.example.paetz.yacguide.utils.IntentConstants;
import com.example.paetz.yacguide.utils.WidgetUtils;

import org.json.JSONException;

import java.io.File;
import java.util.Arrays;

public class TourbookActivity extends AppCompatActivity {

    private enum IOOption {
        eExport,
        eImport
    }

    private final String _FILE_NAME = "yacguide_tourbook.json";

    private AppDatabase _db;
    private int[] _availableYears;
    private int _currentYearIdx;
    private int _maxYearIdx;
    private TourbookExporter _exporter;
    private IOOption _ioOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tourbook);
        this.setTitle("Tourenbuch");

        _db = MainActivity.database;
        _exporter = new TourbookExporter(_db);
        _initYears();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == IntentConstants.RESULT_UPDATED) {
            Toast.makeText(this, "Begehung gelöscht", Toast.LENGTH_SHORT).show();
            _displayContent(_availableYears[_currentYearIdx]);
        }
    }

    public void home(View v) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void goToNextYear(View v) {
        if (++_currentYearIdx <= _maxYearIdx) {
            findViewById(R.id.prevYearButton).setVisibility(View.VISIBLE);
            findViewById(R.id.nextYearButton).setVisibility(_currentYearIdx == _maxYearIdx ? View.INVISIBLE : View.VISIBLE);
            _displayContent(_availableYears[_currentYearIdx]);
        }
    }

    public void goToPreviousYear(View v) {
        if (--_currentYearIdx >= 0) {
            findViewById(R.id.nextYearButton).setVisibility(View.VISIBLE);
            findViewById(R.id.prevYearButton).setVisibility(_currentYearIdx == 0 ? View.INVISIBLE : View.VISIBLE);
            _displayContent(_availableYears[_currentYearIdx]);
        }
    }

    public void export(View v) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.export_dialog);
        _ioOption = IOOption.eExport;

        final RadioButton exportRadioButton = dialog.findViewById(R.id.exportRadioButton);
        final RadioButton importRadioButton = dialog.findViewById(R.id.importRadioButton);
        exportRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportRadioButton.setChecked(true);
                importRadioButton.setChecked(false);
                _ioOption = IOOption.eExport;
            }
        });
        importRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportRadioButton.setChecked(false);
                importRadioButton.setChecked(true);
                _ioOption = IOOption.eImport;
            }
        });
        dialog.findViewById(R.id.externButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (_ioOption == IOOption.eExport && !FilesystemUtils.isExternalStorageWritable()) {
                    Toast.makeText(dialog.getContext(), "SD Karte nicht schreibbar", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (_ioOption == IOOption.eImport && !FilesystemUtils.isExternalStorageReadable()) {
                    Toast.makeText(dialog.getContext(), "SD Karte nicht lesbar", Toast.LENGTH_SHORT).show();
                    return;
                }
                _showFileChooser(dialog, true);
            }
        });
        dialog.findViewById(R.id.internButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _showFileChooser(dialog, false);
            }
        });
        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void _displayContent(int year) {
        final Ascend[] ascends = _db.ascendDao().getAll(year);
        LinearLayout layout = findViewById(R.id.tableLayout);
        layout.removeAllViews();
        ((TextView) findViewById(R.id.yearTextView)).setText(String.valueOf(year));

        int currentMonth, currentDay, currentRegionId;
        currentMonth = currentDay = currentRegionId = -1;
        for (final Ascend ascend : ascends) {
            final int month = ascend.getMonth();
            final int day = ascend.getDay();

            Route route = _db.routeDao().getRoute(ascend.getRouteId());
            Rock rock;
            Sector sector;
            Region region;
            if (route == null) {
                // The database entry has been deleted
                route = _db.createUnknownRoute();
                rock = _db.createUnknownRock();
                sector = _db.createUnknownSector();
                region = _db.createUnknownRegion();
            } else {
                rock = _db.rockDao().getRock(route.getParentId());
                sector = _db.sectorDao().getSector(rock.getParentId());
                region = _db.regionDao().getRegion(sector.getParentId());
            }

            if (month != currentMonth || day != currentDay || region.getId() != currentRegionId) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        day + "." + month + "." + year,
                        region.getName(),
                        WidgetUtils.infoFontSizeDp,
                        null,
                        0xFFBBBBBB,
                        Typeface.BOLD,
                        5, 10, 5, 0));
                layout.addView(WidgetUtils.createHorizontalLine(this, 5));
                currentMonth = month;
                currentDay = day;
                currentRegionId = region.getId();
            }
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TourbookActivity.this, TourbookAscendActivity.class);
                    intent.putExtra(IntentConstants.ASCEND_KEY, ascend.getId());
                    startActivityForResult(intent, 0);
                }
            };
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    rock.getName() + " - " + route.getName(),
                    route.getGrade(),
                    WidgetUtils.tableFontSizeDp,
                    onClickListener,
                    Color.WHITE,
                    Typeface.NORMAL));
            layout.addView(WidgetUtils.createHorizontalLine(this, 1));
        }
    }

    private void _showFileChooser(final Dialog dialog, boolean external) {
        new FileChooser(dialog.getContext(), external).setFileListener(new FileChooser.FileSelectedListener() {
            @Override public void fileSelected(final File file) {
                String filePath = file.getAbsolutePath();
                if (file.isDirectory()) {
                    if (_ioOption == IOOption.eImport) {
                        Toast.makeText(dialog.getContext(), "Keine Datei ausgewählt", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    filePath += "/" + _FILE_NAME;
                }
                _showConfirmDialog(dialog, filePath);
            }
        }).showDialog();
    }

    private void _showConfirmDialog(final Dialog dialog, final String filePath) {
        final Dialog confirmDialog = new Dialog(dialog.getContext());
        confirmDialog.setContentView(R.layout.dialog);
        final String infoText = (_ioOption == IOOption.eExport)
                ? "Dies überschreibt eine bereits vorhandene Datei gleichen Namens.\nTrotzdem exportieren?"
                : "Dies überschreibt das gesamte Tourenbuch.\nTrotzdem importieren?";
        ((TextView) confirmDialog.findViewById(R.id.dialogText)).setText(infoText);
        confirmDialog.findViewById(R.id.yesButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String successMsg;
                    if (_ioOption == IOOption.eExport) {
                        _exporter.exportTourbook(filePath);
                        successMsg = "Tourenbuch erfolgreich exportiert";
                    } else {
                        _exporter.importTourbook(filePath);
                        successMsg = "Tourenbuch erfolgreich importiert";
                    }
                    Toast.makeText(TourbookActivity.this, successMsg, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    Toast.makeText(TourbookActivity.this, "Fehler beim Exportieren", Toast.LENGTH_SHORT).show();
                }
                confirmDialog.dismiss();
                dialog.dismiss();
                _initYears();
            }
        });
        confirmDialog.findViewById(R.id.noButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDialog.dismiss();
            }
        });
        confirmDialog.setCanceledOnTouchOutside(false);
        confirmDialog.setCancelable(false);
        confirmDialog.show();
    }

    private void _initYears() {
        _availableYears = _db.ascendDao().getYears();
        Arrays.sort(_availableYears);
        _currentYearIdx = _maxYearIdx = _availableYears.length - 1;
        if (_currentYearIdx >= 0) {
            findViewById(R.id.nextYearButton).setVisibility(View.INVISIBLE);
            findViewById(R.id.prevYearButton).setVisibility(_availableYears.length > 1 ? View.VISIBLE : View.INVISIBLE);
            _displayContent(_availableYears[_currentYearIdx]);
        }
    }
}
