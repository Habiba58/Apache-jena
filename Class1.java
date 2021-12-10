import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.Vector;

import javax.naming.spi.DirStateFactory.Result;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.VCARD;

import com.github.andrewoma.dexx.collection.Pair;
// 20180084 20180207 20180313
public class Class1 {

	public static void main(String[] args) {
		// create an empty model 
					Model model = ModelFactory.createDefaultModel(); 
					// use the FileManager to find the input file 
					InputStream in = FileManager.get().open( "Ass1.owl" ); 
					if (in == null) { 
						throw new IllegalArgumentException( "File: not found"); } 
					// read the RDF/XML file 
					model.read(in, null); 
					// write it to standard out 
					//model.write(System.out);
					Scanner scanner = new Scanner(System.in);
					System.out.print("Please Enter The patient name ");
					String patientName = scanner.next();
					Query1(patientName, model);
					System.out.print("\n");
					System.out.print("Patients That Have Overlapping Medicines With thier seveirty Level:\n");
					Query2(model);

	}
	public static void Query1(String patientName,Model model) { 
		
		String patientURI = "http://www.semanticweb.org/user/ontologies/2021/11/Assignment1#"+patientName;
		Resource patientResource = model.getResource(patientURI);
		
		String diseaseURI ="http://www.semanticweb.org/user/ontologies/2021/11/Assignment1#Has-Disease";
		Property diseaseProperty=model.createProperty(diseaseURI);
		
		String diseaseNameURI="http://www.semanticweb.org/user/ontologies/2021/11/Assignment1#Has-Name";
		Property diseaseNameProperty = model.createProperty(diseaseNameURI);
		
		StmtIterator diseaseIter = patientResource.listProperties(diseaseProperty);
		
		
		System.out.println(patientName+" Has disease: ");
		while(diseaseIter.hasNext())
		{
			System.out.println(" " + diseaseIter.nextStatement() .getProperty(diseaseNameProperty).getString());
		}
		ArrayList <String> Meds=PatientMedications(patientName, model); // ArrayList That carry The List of disease for each patient
		System.out.println(patientName +" Take medications: ");
		for(int i=0;i<Meds.size();i++) {
			System.out.print(" "+Meds.get(i)+"\n");
		}
		  
	  }  

	public static void Query2(Model model) {
		String Result="";
        
		String PatNameURI="http://www.semanticweb.org/user/ontologies/2021/11/Assignment1#Pat.Name"; 
		Property property=model.createProperty(PatNameURI); 
		ResIterator iter = model.listSubjectsWithProperty(property); 
		while (iter.hasNext()) { 
			String patientname = iter.nextResource().getProperty(property) .getString();
			ArrayList<String> Medications=PatientMedications(patientname, model);
			ArrayList<ArrayList<String>> dates = new ArrayList<ArrayList<String>>();
			for(int i = 0; i < Medications.size(); i++) {
				ArrayList<String> DATE=DatesOfDrug(Medications.get(i),model);
				dates.add(DATE);	
			}
			
			for(int i = 0; i < dates.size(); i++) {
				for(int j = i+1; j < dates.size(); j++) {
					if(OverlappingTesting(dates.get(i).get(0), dates.get(j).get(0), dates.get(i).get(1), dates.get(j).get(1))) {
			     Result = Interaction(model,Medications.get(i),Medications.get(j));	
			     
					}
					System.out.println(Result);	
				}
			}
			
			}
		
		}
	public static String Interaction(Model model, String FirstMedicine,String SecondMedicine) { // Check the severity Level Between the overlapping medicines
		
		
		String SeverityLevel="";
		String Medicine1URL="http://www.semanticweb.org/user/ontologies/2021/11/Assignment1#" + FirstMedicine; 
		String MedicationNameURI = "http://www.semanticweb.org/user/ontologies/2021/11/Assignment1#Med.Name";
		String majorLevelURI="http://www.semanticweb.org/user/ontologies/2021/11/Assignment1#Major";
		String moderateLevelURI = "http://www.semanticweb.org/user/ontologies/2021/11/Assignment1#Moderate";
		String minorLevelURI = "http://www.semanticweb.org/user/ontologies/2021/11/Assignment1##Minor";
		
		Property drugNameProperty = model.createProperty(MedicationNameURI);
		Property majorProperty = model.createProperty(majorLevelURI); 
		Property moderateProperty = model.createProperty(moderateLevelURI); 
		Property minorProperty = model.createProperty(minorLevelURI); 
		Resource Medicine1 = model.getResource(Medicine1URL); 
		StmtIterator iterMajor = Medicine1.listProperties(majorProperty);
		StmtIterator iterModerate = Medicine1.listProperties(moderateProperty);
		StmtIterator iterMinor = Medicine1.listProperties(minorProperty);
		
		while(iterMajor.hasNext()) { 
			String name = iterMajor.nextStatement() .getProperty(drugNameProperty).getString();
			if(name == SecondMedicine) {
				SeverityLevel += " " + FirstMedicine + " Major " + SecondMedicine + " \n";
			}
		}
		
		
		while(iterModerate.hasNext()) { 
			String name = iterModerate.nextStatement() .getProperty(drugNameProperty).getString();
			if(name == SecondMedicine) {
				SeverityLevel += (" " + FirstMedicine + " Moderate " + SecondMedicine + " \n");
			}
		}
	
		while(iterMinor.hasNext()) { 
			String name = iterMinor.nextStatement() .getProperty(drugNameProperty).getString();
			if(name == SecondMedicine) {
				SeverityLevel += (" " + FirstMedicine + " Minor "  + SecondMedicine+"\n"  );
			}
		}

		return SeverityLevel;
		
	}
	public static Boolean OverlappingTesting(String startA, String startB, String endA, String endB) { // Test If their exist any medicines that overlap with each other
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");  
		Date dateSA;
		try {
			dateSA = sdf.parse(startA);
			Date dateEA = sdf.parse(endA); 
			Date dateSB = sdf.parse(startB);  
			Date dateEB = sdf.parse(endB); 
			return dateSA.compareTo(dateEB) <= 0 && dateEA.compareTo(dateSB) >= 0;
		} catch (ParseException e) {
			e.printStackTrace();
		}  
		return false;
	} 
	public static ArrayList<String> DatesOfDrug(String Medication, Model model) { // given a specific drug brings the start date and the end date of this drug
		ArrayList<String> Dates = new ArrayList<>(); 
		String MedicationURI="http://www.semanticweb.org/user/ontologies/2021/11/Assignment1#" + Medication; 
		String StartDateURI="http://www.semanticweb.org/user/ontologies/2021/11/Assignment1#StartDate";
		String EndDateURI = "http://www.semanticweb.org/user/ontologies/2021/11/Assignment1#EndDate";
		
		Property endDateProperty = model.createProperty(EndDateURI); 
		Property startDateProperty = model.createProperty(StartDateURI); 
		
		Resource Med = model.getResource(MedicationURI); 
		
		StmtIterator StartDATEIter = Med.listProperties(startDateProperty);
		StmtIterator EndDATEIter = Med.listProperties(endDateProperty);
		Dates.add(StartDATEIter.nextStatement().getLiteral().getString());
		Dates.add(EndDATEIter.nextStatement().getLiteral().getString());
		return Dates;
		
					
	}
	
	public static ArrayList<String> PatientMedications(String name,Model model){ // given a patient name lists the drugs they take
		ArrayList<String> Medications = new ArrayList<>();
		
		String patientURI = "http://www.semanticweb.org/user/ontologies/2021/11/Assignment1#"+name; 
		String takeMedicationURI="http://www.semanticweb.org/user/ontologies/2021/11/Assignment1#Take-Medicatin"; 
		String medName= "http://www.semanticweb.org/user/ontologies/2021/11/Assignment1#Med.Name";
		
		Property takeMedicationProperty = model.createProperty(takeMedicationURI); 
		Property medNameProperty = model.createProperty(medName); 
		
		Resource patientResource = model.getResource(patientURI); 
		
		StmtIterator medIter = patientResource.listProperties(takeMedicationProperty); 
		
		while(medIter.hasNext()) { 
			String Medname=medIter.nextStatement().getProperty(medNameProperty).getString();
			
			Medications.add(Medname);
			
			}

		return Medications;
		
	}

}
