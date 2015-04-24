package com.example.daw.tri.Library;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.daw.tri.Objects.Day;
import com.example.daw.tri.Objects.Presentation;
import com.example.daw.tri.Objects.Section;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by EN on 11.4.2015.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static String DB_PATH = "/data/data/com.example.daw.tri/databases/";

    private static String DB_NAME = "tri";

    private SQLiteDatabase myDataBase;

    private final Context myContext;

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public DatabaseHandler(Context context) {

        super(context, DB_NAME, null, 1);

        this.myContext = context;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if(dbExist){
            //do nothing - database already exist
        }else{

            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getWritableDatabase();

            try {

                copyDataBase();

            } catch (IOException e) {
                throw new Error("Error copying database");

            }
        }

    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){
        SQLiteDatabase checkDB = null;
        try{
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        }catch(SQLiteException e){
            //database does't exist yet.
        }
        if(checkDB != null){
            checkDB.close();
        }
        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException {

        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public void openDataBase() throws SQLException {
        //Open the database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {
        if(myDataBase != null)
            myDataBase.close();

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void addColumn() throws SQLException {
        openDataBase();
        myDataBase.rawQuery("ALTER TABLE personal ADD COLUMN time_to TEXT",null);
        myDataBase.rawQuery("ALTER TABLE personal ADD COLUMN time_from TEXT",null);
        myDataBase.close();
    }


    public void dropAll(){
        myDataBase.execSQL("delete from day");
        myDataBase.execSQL("delete from section");
        myDataBase.execSQL("delete from presentation");
        myDataBase.execSQL("delete from hall");
        myDataBase.execSQL("delete from personal_presentation");
    }

    public void insertDay(int id, String date) {
        Log.i("daw", date);
        ContentValues values = new ContentValues();
        try {
            this.openDataBase();
            values.put("id", id);
            values.put("day", date);
            myDataBase.insert("day", null, values);
            myDataBase.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<Day> selectDay() throws SQLException, ParseException {
        openDataBase();
        Cursor see = myDataBase.rawQuery("SELECT * FROM day ",null);
        ArrayList<Day> listOfDays = new ArrayList<Day>();
        Day tmp;
        see.moveToFirst();
        while (!see.isAfterLast()) {
            tmp = new Day(see.getLong(0), see.getString(1));
            listOfDays.add(tmp);
            see.moveToNext();
        }
        see.close();
        myDataBase.close();
        return listOfDays;
    }

    public ArrayList<Section> selectSectionByDay(Long id) throws SQLException, ParseException {
        openDataBase();
        Cursor see = myDataBase.rawQuery("SELECT * FROM section WHERE id_day="+id,null);
        ArrayList<Section> listOfSections = new ArrayList<Section>();
        Section tmp;
        see.moveToFirst();
        while (!see.isAfterLast()) {
            tmp = new Section(see.getLong(0), see.getLong(1),see.getLong(2),see.getString(3));
            listOfSections.add(tmp);
            see.moveToNext();
        }
        see.close();
        myDataBase.close();
        return listOfSections;
    }

    public ArrayList<Presentation> selectPresentationById(Long id) throws SQLException, ParseException {
        openDataBase();
        Cursor see = myDataBase.rawQuery("SELECT * FROM presentation WHERE id_section="+id,null);
        ArrayList<Presentation> listOfPresentations = new ArrayList<>();
        Presentation tmp;
        see.moveToFirst();
        while (!see.isAfterLast()) {
            tmp = new Presentation(see.getLong(0), see.getString(2),see.getString(3));
            listOfPresentations.add(tmp);
            see.moveToNext();
        }
        see.close();
        myDataBase.close();
        return listOfPresentations;
    }

    public void insertPersonalSection(Long idSection) throws SQLException, ParseException {
        openDataBase();
        Cursor see = myDataBase.rawQuery("SELECT day,time_to, time_from FROM section,day WHERE section.id="+idSection+" AND section.id_day=day.id",null);
        see.moveToFirst();
        ContentValues values = new ContentValues();
        values.put("id", idSection);
        String time_to = see.getString(0)+" "+ see.getString(1);
        String time_from = see.getString(0)+" "+ see.getString(2);
        values.put("time_to",time_to);
        values.put("time_from",time_from);
        myDataBase.insert("personal", null, values);
        myDataBase.close();

    }

    public List<String> getPersonalList() throws SQLException, ParseException {
        List<String> result = new ArrayList<>();
        openDataBase();
        Cursor see,pointer;
        see = myDataBase.rawQuery("SELECT id FROM personal ORDER BY time_from", null);
        see.moveToFirst();
        while (!see.isAfterLast()){
            pointer = myDataBase.rawQuery("SELECT name FROM section WHERE id="+see.getLong(0),null);
            pointer.moveToFirst();
            result.add(pointer.getString(0));
            see.moveToNext();
        }
        myDataBase.close();
        return result;
    }

    public boolean isSectionInPersonal(Long idSection) throws SQLException {
        openDataBase();
        Cursor see = myDataBase.rawQuery("SELECT id FROM personal WHERE id="+idSection,null);
        see.moveToFirst();
        return see.getCount() > 0;
    }

    public void removeSectionFromPersonal(Long id) throws SQLException {
        openDataBase();
        myDataBase.execSQL("DELETE FROM personal WHERE id=" + id);
        myDataBase.close();
    }

    public void renewPersonal() throws SQLException, ParseException {
        List<Long> idArray = new ArrayList<>();
        openDataBase();
        Cursor see = myDataBase.rawQuery("SELECT id FROM personal",null);
        see.moveToFirst();
        while(!see.isAfterLast()){
            idArray.add(see.getLong(0));
            removeSectionFromPersonal(see.getLong(0));
            see.moveToNext();
        }
        for(Long id : idArray){
            insertPersonalSection(id);
        }
    }

    public String checkPersonalForCollisions() throws SQLException, ParseException {
        openDataBase();
        String result = "";
        Long section1, section2;
        String section1Name,section2Name;
        Cursor see = myDataBase.rawQuery("SELECT personal.id, section.name FROM personal,section WHERE section.id = personal.id ORDER BY personal.time_to",null);
        see.moveToFirst();
        while(!see.isAfterLast()){
            section1Name = see.getString(1);
            section1 = see.getLong(0);
            see.moveToNext();
            if(!see.isAfterLast()){
                section2Name = see.getString(1);
                section2 = see.getLong(0);
                if(isSectionCollision(section1,section2)){
                    result +="There is a program collision between "+section1Name+" and "+section2Name+"!\n";
                }
            }
        }
        myDataBase.close();
        return result;
    }

    public void insertPersonalPresentation(Long idPresentaiton) throws SQLException {
        openDataBase();
        ContentValues values = new ContentValues();
        values.put("id", idPresentaiton);
        myDataBase.insert("personal_presentation",null,values);
        myDataBase.close();
    }

    public void removePresentationFromPersonal(Long idPresentation) throws SQLException {
        openDataBase();
        myDataBase.execSQL("DELETE FROM personal_presentation WHERE id=" + idPresentation);
        myDataBase.close();
    }

    public boolean isPresentationInPersonal(Long idPresentation) throws SQLException {
        openDataBase();
        Cursor see = myDataBase.rawQuery("SELECT id FROM personal_presentation WHERE id="+idPresentation,null);
        see.moveToFirst();
        Log.i("presentation/boolean: ",Long.toString(idPresentation)+", "+Integer.toString(see.getCount()));
        return see.getCount() > 0;
    }

    public Long getNthPresentation(Long idSection, int presentationPosition) throws SQLException {
        openDataBase();
        Cursor see = myDataBase.rawQuery("SELECT presentation.id FROM presentation, section WHERE presentation.id_section=section.id AND section.id="+idSection+" LIMIT "+presentationPosition+",1",null);
        see.moveToFirst();
        return see.getLong(0);
    }

    public void removePresentationsFromPersonalBySection(Long sectionId) throws SQLException {
        openDataBase();
        Cursor see = myDataBase.rawQuery("SELECT id FROM presentation WHERE id_section="+sectionId,null);
        see.moveToFirst();
        while(!see.isAfterLast()){
            if (isPresentationInPersonal(see.getLong(0))){
                removePresentationFromPersonal(see.getLong(0));
            }
            see.moveToNext();
        }
    }

    public boolean isSectionCollision(Long section1, Long section2) throws SQLException, ParseException {
        Date section1To,section2From;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Cursor see = myDataBase.rawQuery("SELECT time_from,time_to FROM personal WHERE id="+section1+" OR id="+section2+" ORDER BY time_from", null);
        see.moveToFirst();
        section1To = dateFormat.parse(see.getString(1));
        see.moveToNext();
        section2From = dateFormat.parse(see.getString(0));
        return section2From.before(section1To);
    }

    public List<String> getSectionList(Long day) throws SQLException {
        openDataBase();
        List<String> result = new ArrayList<>();
        Cursor see = myDataBase.rawQuery("SELECT name FROM section WHERE id_day="+day,null);
        see.moveToFirst();
        while(!see.isAfterLast()){
            result.add(see.getString(0));
            see.moveToNext();
        }
        close();
        return result;
    }

    public HashMap<String,List<String>> getSectionPresentationMap(Long day) throws SQLException {
        openDataBase();
        HashMap<String,List<String>> result = new HashMap<>();
        Cursor pointer;
        Cursor see  = myDataBase.rawQuery("SELECT id,name FROM section WHERE id_day="+day,null);
        see.moveToFirst();
        while(!see.isAfterLast()){
            List<String> presentations = new ArrayList<>();
            pointer = myDataBase.rawQuery("SELECT name FROM presentation WHERE id_section="+see.getLong(0),null);
            pointer.moveToFirst();
            while (!pointer.isAfterLast()){
                presentations.add(pointer.getPosition()+1+". "+pointer.getString(0));
                pointer.moveToNext();
            }
            result.put(see.getString(1),presentations);
            see.moveToNext();
        }
        close();
        return result;
    }

    public HashMap<String,List<String>> getPersonalPresentationMap() throws SQLException, ParseException {
        openDataBase();
        HashMap<String,List<String>> result = new HashMap<>();
        Cursor pointer;
        Cursor see  = myDataBase.rawQuery("SELECT personal.id,section.name, personal.time_from,personal.time_to FROM section, personal WHERE personal.id=section.id",null);
        see.moveToFirst();
        while(!see.isAfterLast()){
            List<String> presentations = new ArrayList<>();
            SimpleDateFormat daysHours = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date time_from = daysHours.parse(see.getString(2));
            Date time_to = daysHours.parse(see.getString(3));
            daysHours = new SimpleDateFormat("hh:mm dd.MM.");
            presentations.add("Begin: "+ daysHours.format(time_from));
            presentations.add("End: "+ daysHours.format(time_to));
        /* Display all of the presentations by section
        while(!see.isAfterLast()){
            List<String> presentations = new ArrayList<>();
            pointer = myDataBase.rawQuery("SELECT name FROM presentation WHERE id_section="+see.getLong(0),null);
            pointer.moveToFirst();
            while (!pointer.isAfterLast()){
                presentations.add(pointer.getString(0));
                pointer.moveToNext();
            }*/
            result.put(see.getString(1),presentations);
            see.moveToNext();
        }
        myDataBase.close();
        return result;
    }

    public Long getNthSection(int position) throws SQLException {
        openDataBase();
        Cursor see = myDataBase.rawQuery("SELECT id FROM section LIMIT "+position+",1",null);
        see.moveToFirst();
        close();
        return see.getLong(0);
    }

    public Long getNthSectionFromPersonal(int position) throws SQLException {
        openDataBase();
        Cursor see = myDataBase.rawQuery("SELECT id FROM personal ORDER BY time_from LIMIT "+position+",1",null);
        see.moveToFirst();
        close();
        return see.getLong(0);
    }

    public void insertHall(int id, String name){
        ContentValues values = new ContentValues();
        Log.i("hall test", name);
        try {
            this.openDataBase();
            values.put("id", id);
            values.put("name", name);
            myDataBase.insert("hall", null, values);
            myDataBase.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertPresentation(int id, int id_section,String name, String author){
        ContentValues values = new ContentValues();
        try {
            this.openDataBase();
            values.put("id", id);
            values.put("id_section", id_section);
            values.put("name", name);
            values.put("author", author);
            myDataBase.insert("presentation", null, values);
            myDataBase.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertSection(int id,int id_hall,int id_day,String name,String chairman, String time_from, String time_to, String type){
        ContentValues values = new ContentValues();
        try {
            this.openDataBase();
            values.put("id", id);
            values.put("id_hall", id_hall);
            values.put("id_day", id_day);
            values.put("name", name);
            values.put("chairman", chairman);
            values.put("time_from", time_from);
            values.put("time_to", time_to);
            values.put("type", type);
            myDataBase.insert("section", null, values);
            myDataBase.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
