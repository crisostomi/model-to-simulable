package SimulableModel;

public class LinkSimulableSpeciesComprises {
    private LinkSimulableSpeciesComprises(){}


    public static void insertLink(SimulableModel model, SimulableSpecies species) throws PreconditionsException {
        // Creo un pass: solo i metodi di questa classe possono farlo!
        LinkSimulableSpeciesComprises pass = new LinkSimulableSpeciesComprises();
        // Creo il link
        LinkTypeSimulableSpeciesComprises link = new LinkTypeSimulableSpeciesComprises(model, species);
        // Inserisco il link nei due oggetti esibendo il pass
        model.insertLinkSimulableSpeciesComprises(pass, link);
        species.insertLinkSimulableSpeciesComprises(pass, link);
    }

    public static void removeLink(LinkTypeSimulableSpeciesComprises link)
            throws PreconditionsException {
        LinkSimulableSpeciesComprises pass = new LinkSimulableSpeciesComprises();
        link.getSimulableModel().removeLinkSimulableSpeciesComprises(pass, link);
        link.getSimulableSpecies().removeLinkSimulableSpeciesComprises(pass);
    }
    
}
