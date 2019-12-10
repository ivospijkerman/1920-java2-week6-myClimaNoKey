package com.soricosoft.climapm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.ImageButton;

public class ChangeCityController extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_city_layout);

        final EditText editTextField = findViewById(R.id.queryET);
        ImageButton backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        editTextField.setOnEditorActionListener((v, actionId, event) -> {

            String newCity = editTextField.getText().toString();
            Intent newCityIntent = new Intent(ChangeCityController.this, WeatherController.class);
            newCityIntent.putExtra("City", newCity);

            startActivity(newCityIntent);

            return false;
        });
    }
}
