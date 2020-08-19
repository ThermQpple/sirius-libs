
package fragtreealigner.domainobjects.chem.components;

import fragtreealigner.domainobjects.chem.basics.MolecularFormula;
import fragtreealigner.domainobjects.chem.structure.MolecularStructure;
import fragtreealigner.util.Session;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class ChemicalComponent implements Serializable {
	protected String name;
	protected double mass;
	protected MolecularFormula molecularFormula;
	protected MolecularStructure molecularStructure;
	protected Session session;

	public ChemicalComponent() {}

	public ChemicalComponent(String name, double mass) {
		this.setName(name);
		this.setMass(mass);
	}	
	
	public ChemicalComponent(String name, double mass, Session session) {
		this(name, mass);
		this.session = session;
	}	

	public ChemicalComponent(String name, double mass, MolecularFormula molecularFormula, Session session) {
		this(name, mass, session);
		this.molecularFormula = molecularFormula;
	}
	
	public ChemicalComponent(String name, double mass, String molFormulaStr, Session session) {
		this(name, mass, new MolecularFormula(molFormulaStr, session), session);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	public double getMass() {
		return mass;
	}

	public void setMolecularFormula(MolecularFormula molecularFormula) {
		this.molecularFormula = molecularFormula;
	}

	public MolecularFormula getMolecularFormula() {
		return molecularFormula;
	}

	public void setMolecularStructure(MolecularStructure molecularStructure) {
		this.molecularStructure = molecularStructure;
	}

	public MolecularStructure getMolecularStructure() {
		return molecularStructure;
	}

	public Session getSession() {
		return session;
	}

	public int size() {
		return this.getMolecularFormula().size();
	}
	
	public MolecularFormula diff(ChemicalComponent chemComponent) {
		return this.getMolecularFormula().diff(chemComponent.getMolecularFormula());
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( obj == null ) return false; 
		if ( obj == this ) return true; 
		if ( ! obj.getClass().equals(getClass()) ) return false; 
		
		ChemicalComponent chemicalComponent = (ChemicalComponent) obj; 
		return this.getMolecularFormula().equals(chemicalComponent.getMolecularFormula());
	}

	public int symmetricDifference(ChemicalComponent other) {
		int[] array1 = getMolecularFormula().getNumberOfAtomsAsList();
		int[] array2 = other.getMolecularFormula().getNumberOfAtomsAsList();
		int posOfH = session.getParameters().elementTable.getElementList().indexOf("H");
		int result = 0;		
		// symmDiff as union minus intersection
		for (int i = 0; i<array1.length; ++i){
			if (i!= posOfH){
				result += Math.abs(array1[i]-array2[i]);
			}
		}
		return result;
	}

//	public void builUpClonedChemicalComponent(ChemicalComponent clonedChemComponent) {
//	}
}
