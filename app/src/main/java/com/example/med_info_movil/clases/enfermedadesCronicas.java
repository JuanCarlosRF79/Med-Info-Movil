package com.example.med_info_movil.clases;

public class enfermedadesCronicas {
    private String[] enfermedades;

    public void enfermedadesCronicas(){
        enfermedades = new String[]{};
    }

    public String[] obtenerEnfermedades(){
        return enfermedades = new String[]{
                "Colesterol alto", "Hipertensión", "Enfermedad pulmonar", "Alzheimer",
                "Osteoporosis", "Septicemia", "Apoplejia (derrame)/Ataque Cerebral",
                "Diabetes", "Ansiedad", "Trastorno por déficit de atención con hiperactividad",
                "Autismo", "Trastorno Bipolar", "Demencia", "Depresión", "Transtorno Alimenticio",
                "Trastorno Obsesivo Compulsivo", "Trastorno de Pánico", "Trastorno de la personalidad",
                "Esquizofrenia", "Asma", "Bronquitis crónica", "Enfisema", "Neumonía",
                "Enfermedad renal quística", "Enfermedad Renal Diabética", "Nefritis", "Nefrosis de riñón",
                "Cáncer de los huesos", "Cáncer de mama", "Cáncer de colon", "Cáncer de esófago",
                "Cáncer gástrico", "Cáncer de riñón", "Leucemia", "Enfermedad de Crohn", "Angina"
        };
    }

}
