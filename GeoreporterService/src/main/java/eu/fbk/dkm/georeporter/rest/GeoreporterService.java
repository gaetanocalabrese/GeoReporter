package eu.fbk.dkm.georeporter.rest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.plaf.metal.MetalBorders.Flush3DBorder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

import com.sun.jersey.api.json.JSONWithPadding;
import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;

import eu.fbk.dkm.georeporter.pojos.Attributo;
import eu.fbk.dkm.georeporter.pojos.FornituraEnergia;
import eu.fbk.dkm.georeporter.pojos.FornituraGas;
import eu.fbk.dkm.georeporter.pojos.FornituraLocazioni;
import eu.fbk.dkm.georeporter.pojos.MappingTabella;
import eu.fbk.dkm.georeporter.pojos.Particella;
import eu.fbk.dkm.georeporter.pojos.Soggetto;
import eu.fbk.dkm.georeporter.pojos.TributiICI;
import eu.fbk.dkm.georeporter.pojos.UnitaImmobiliare;
import eu.fbk.dkm.georeporter.pojos.UtenzaAcqua;
import eu.fbk.dkm.georeporter.pojos.UtenzaRifiuti;
import eu.fbk.dkm.georeporter.utils.Costanti;

/**
 * GeoreporterService e' un servizio REST che espone i metodi per gestire la
 * knowledge base Fornisce inoltre una serie di metodi per recuperare le
 * informazioni
 * 
 * @author Gaetano Calabrese,
 *
 */
@Path("/rest")
public class GeoreporterService {

	String springlesserverURL = "http://localhost:8080/openrdf-sesame";
	String springlesrepositoryID = "georeporter";
	String uricontesto = "http://dkm.fbk.eu/ckr/meta#global";

	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	String queryStringPrefix = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" + "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"
			+ "PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n" + "PREFIX ckr:<http://dkm.fbk.eu/ckr/meta#> \n"
			+ "PREFIX meta:<http://dkm.fbk.eu/ckr/meta#>\n" + "PREFIX  :<http://dkm.fbk.eu/georeporter#>\n";

	public GeoreporterService() {
		super();
	}

	/**
	 * Method to test Springles Service - url :
	 * http://localhost:8080/GeoreporterService/rest/hallo
	 * 
	 * @return message to a web page
	 */
	@GET
	@Path("/hallo")
	@Produces(MediaType.TEXT_PLAIN)
	public String sayPlainTextHello() {
		return "Hello!It's SpringlesService";
	}

	/**
	 * Metodo per inserire i dati relativi ad una fornitura di Gas url :
	 * http://localhost:8080/GeoreporterService/rest/forniruregas
	 * 
	 * @return message to a web page
	 */
	@POST
	@Path("/fornituregas")
	@Produces(MediaType.APPLICATION_JSON)
	public void setFornitureGas(

			@QueryParam("ammontarefatturato") String ammontarefatturato,
			@QueryParam("consumofatturato") String consumofatturato, @QueryParam("tipoutenzagas") String tipoutenzagas,
			@QueryParam("idsiatelgas") String idsiatelgas,
			@QueryParam("idsiatelgasdettaglio") String idsiatelgasdettaglio,
			@QueryParam("Denominazione") String denominazione, @QueryParam("CodComune") String codcomune,
			@QueryParam("AnnoRiferimento") String annoriferimento,
			@QueryParam("CodFiscaleTitolareUtenza") String CodFiscaleTitolareUtenza,
			@QueryParam("TipoPersona") String TipoPersona, @QueryParam("DatiAnagrafici	") String DatiAnagrafici,
			@QueryParam("TipoUtenzaEnergia	") String TipoUtenzaEnergia,
			@QueryParam("IndirizzoUtenza	") String IndirizzoUtenza,
			@QueryParam("SpesaConsumo	") String SpesaConsumo, @QueryParam("KWhFatturato	") String KWhFatturato,
			@QueryParam("NumeroMesiFatturazione	") String NumeroMesiFatturazione,
			@QueryParam("CAPUtenza") String CAPUtenza) {

		// if ((hasTerm==null)||hasTerm.equals("")){

		ResponseBuilderImpl builder = new ResponseBuilderImpl();
		builder.status(Response.Status.BAD_REQUEST);
		builder.entity("ERROR! term parameter missing or null");
		Response response = builder.build();
		throw new WebApplicationException(response);

		// }

	}

	/**
	 * Metodo per inserire i dati relativi ad una fornitura di Energia url :
	 * http://localhost:8080/GeoreporterService/rest/fornirureenergia
	 * 
	 * @return message to a web page
	 */
	@POST
	@Path("/fornitureenergia")
	@Produces(MediaType.APPLICATION_JSON)
	public void setFornitureEnergia(@QueryParam("idsiatelenergisdettaglio") String idsiatelenergisdettaglio,
			@QueryParam("ammontarefatturato") String ammontarefatturato,
			@QueryParam("consumofatturato") String consumofatturato,
			@QueryParam("tipoutenzaenergia") String tipoutenzaenergia,
			@QueryParam("idsiatelenergia") String idsiatelenergia, @QueryParam("Denominazione") String denominazione,
			@QueryParam("CodComune") String codcomune, @QueryParam("AnnoRiferimento") String annoriferimento,
			@QueryParam("CodFiscaleTitolareUtenza") String CodFiscaleTitolareUtenza,
			@QueryParam("TipoPersona") String TipoPersona, @QueryParam("DatiAnagrafici	") String DatiAnagrafici,
			@QueryParam("TipoUtenzaEnergia	") String TipoUtenzaEnergia,
			@QueryParam("IndirizzoUtenza	") String IndirizzoUtenza,
			@QueryParam("SpesaConsumo	") String SpesaConsumo, @QueryParam("KWhFatturato	") String KWhFatturato,
			@QueryParam("NumeroMesiFatturazione	") String NumeroMesiFatturazione,
			@QueryParam("IdSiatelEnergia") String IdSiatelEnergia, @QueryParam("CAPUtenza") String CAPUtenza

	) {

		// if ((hasTerm==null)||hasTerm.equals("")){

		ResponseBuilderImpl builder = new ResponseBuilderImpl();
		builder.status(Response.Status.BAD_REQUEST);
		builder.entity("ERROR! term parameter missing or null");
		Response response = builder.build();
		throw new WebApplicationException(response);

		// }

	}

	/**
	 * Metodo che restituisce la lista di tutte le particelle
	 * 
	 * <p>
	 * SELECT ?particella ?ui " WHERE { " ?x a :IdentificativoCatastale . ?x
	 * :hasParticella ?particella . ?x :hasUnitaImmobiliare ?ui } ORDER by
	 * ?particella
	 * </p>
	 * url : http://localhost:8080/GeoreporterService/rest/hallo
	 * 
	 * @return message to a web page
	 */
	@GET
	@Path("/particelle")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Particella> getParticelle() {

		// @QueryParam("springlesserverURL") String springlesserverURL,
		// @QueryParam("springlesrepositoryID") String springlesrepositoryID
		// ) {

		// String springlesrepositoryID ="georeporter";

		List<BindingSet> tuples = new ArrayList<BindingSet>();
		List<Particella> listaParticella = new ArrayList<Particella>();

		Repository myRepository = new HTTPRepository(springlesserverURL, springlesrepositoryID);
		try {
			myRepository.initialize();

			RepositoryConnection connection = myRepository.getConnection();

			String queryString = queryStringPrefix

					+ "SELECT DISTINCT ?particella ?ui ?comunecatastale ?numero  ?foglio ?denominatore " + "WHERE { "
					+ "    ?x a :IdentificativoCatastale . " + "    ?x :hasParticella ?particella . "
					+ "    ?x :hasUnitaImmobiliare ?ui ."

					+ "    ?particella :numero ?numero . "
					+ "    ?particella :codicecomunecatastale ?comunecatastale . "
					+ " OPTIONAL{   ?particella :foglio ?foglio  }. "
					+ " OPTIONAL{   ?particella :denominatore ?denominatore  } . "

					+ "}  ";

			System.out.println(queryString);
			TupleQuery tupleQuery;

			int i = 0;

			tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

			TupleQueryResult qresult = tupleQuery.evaluate();

			while (qresult.hasNext()) {
				BindingSet bindingSet = qresult.next();

				Value uriparticella = bindingSet.getValue("particella");
				Value uriUI = bindingSet.getValue("ui");
				Value numero = bindingSet.getValue("numero");
				Value denominatore = bindingSet.getValue("denominatore");
				Value foglio = bindingSet.getValue("foglio");
				Value comunecatastale = bindingSet.getValue("comunecatastale");

				Particella particella = new Particella();

				if (uriparticella != null) {
					particella.setUri(uriparticella.stringValue());
				}

				if (uriUI != null) {
					particella.setUi(uriUI.stringValue());
				}
				if (foglio != null) {
					particella.setFoglio(foglio.stringValue());
				}
				if (numero != null) {
					particella.setNumero(numero.stringValue());
				}
				if (denominatore != null) {
					particella.setDenomintore(denominatore.stringValue());
				}
				if (comunecatastale != null) {
					particella.setComuneCatastale(comunecatastale.stringValue());
				}

				listaParticella.add(particella);
			}
			qresult.close();
			connection.close();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}

		return listaParticella;
	}

	@GET
	@Path("/unitaimmobiliari_su_particella")
	@Produces(MediaType.APPLICATION_JSON)
	// @Produces({"application/javascript"})

	// public JSONWithPadding getUnitaImmobiliariSuParticella(
	public List<UnitaImmobiliare> getUnitaImmobiliariSuParticella(

			@QueryParam("particella") String particella, @QueryParam("callback") String callback)

	{
		List<UnitaImmobiliare> listaUnitaImmobiliari = new ArrayList<UnitaImmobiliare>();
		Soggetto sog = new Soggetto();
		// String springlesserverURL = "http://localhost:8080/openrdf-sesame";
		// String springlesrepositoryID ="georeporter";

		List<BindingSet> tuples = new ArrayList<BindingSet>();

		/// Soggetto soggetto =new Soggetto();

		Repository myRepository = new HTTPRepository(springlesserverURL, springlesrepositoryID);
		try {
			myRepository.initialize();

			RepositoryConnection connection = myRepository.getConnection();

			String queryString = queryStringPrefix

					+ " select   ?ui ?classe ?piano ?interno ?scala ?categoria ?titolare ?superficie" + " where { "
					+ "    ?x a :IdentificativoCatastale. " + "    ?x :hasParticella :" + particella + " . "
					+ "    ?x :hasUnitaImmobiliare ?ui ." + "     OPTIONAL{ ?ui :classe ?classe }."
					+ "     OPTIONAL{ ?ui :piano1 ?piano}. " + "    OPTIONAL{  ?ui :superficie ?superficie}. "
					+ "   OPTIONAL{  ?ui :interno ?interno}. " + "   OPTIONAL{    ?ui :scala ?scala}. "
					+ "    OPTIONAL{   ?ui :categoria ?categoria}. " + "     OPTIONAL{  ?ui :titolare ?titolare}. "

					+ "	} ";

			System.out.println(queryString);
			TupleQuery tupleQuery;

			int i = 0;

			tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

			TupleQueryResult qresult = tupleQuery.evaluate();

			while (qresult.hasNext()) {
				BindingSet bindingSet = qresult.next();

				Value uiuri = bindingSet.getValue("ui");
				Value piano = bindingSet.getValue("piano");
				Value scala = bindingSet.getValue("scala");
				Value interno = bindingSet.getValue("interno");
				Value categoria = bindingSet.getValue("categoria");
				Value titolare = bindingSet.getValue("titolare");
				Value superficie = bindingSet.getValue("superficie");

				UnitaImmobiliare ui = new UnitaImmobiliare();

				if (uiuri != null) {
					// ui.setUri(uiuri.stringValue());
					// ui.setUi(uiuri.stringValue().substring(u));
					ui.setUi(uiuri.stringValue().substring(uiuri.stringValue().lastIndexOf('#') + 1));
				}

				if (piano != null) {
					ui.setPiano(piano.stringValue());
				}

				if (scala != null) {
					ui.setScala((scala.stringValue()));
				}
				if (interno != null) {
					ui.setInterno((interno.stringValue()));
				}
				if (categoria != null) {
					ui.setCategoria((categoria.stringValue()));
				}
				if (titolare != null) {
					ui.setTitolare((titolare.stringValue()));
				}
				if (superficie != null) {
					ui.setSuperficie((superficie.stringValue()));
				}

				listaUnitaImmobiliari.add(ui);
			}
			// qresult.close();
			// connection.close();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}
		// return new JSONWithPadding(new
		// GenericEntity<List<UnitaImmobiliare>>(listaUnitaImmobiliari){},
		// callback);

		return listaUnitaImmobiliari;
	}

	@GET
	@Path("/anagraficasoggettoui")
	// @Produces(MediaType.APPLICATION_JSON)
	@Produces({ "application/javascript" })
	// public List<Soggetto> getAnagraficaSoggettoui(@QueryParam("ui") String ui) {
	public JSONWithPadding getAnagraficaSoggettoui(@QueryParam("callback") String callback,
			@QueryParam("ui") String ui) {

		Soggetto sog = new Soggetto();
		// String springlesserverURL = "http://localhost:8080/openrdf-sesame";

		// String springlesrepositoryID ="georeporter";

		List<BindingSet> tuples = new ArrayList<BindingSet>();
		List<Soggetto> listaSoggetto = new ArrayList<Soggetto>();
		/// Soggetto soggetto =new Soggetto();

		Repository myRepository = new HTTPRepository(springlesserverURL, springlesrepositoryID);
		try {
			myRepository.initialize();

			RepositoryConnection connection = myRepository.getConnection();

			String queryString = queryStringPrefix

					+ " select    ?soggetto ?tiposoggetto ?codicefiscale ?nome ?cognome ?denominazioneita "
					+ " where { " + "    ?x a :Titolarita ." + "   ?x :hasSoggetto ?soggetto . "
					+ "  ?x :hasIdentificativoCatastale :" + ui + " . " + " ?soggetto :tiposoggetto ?tiposoggetto . "
					+ " OPTIONAL{ ?soggetto :codicefiscale ?codicefiscale } . "
					+ " OPTIONAL{ ?soggetto :denominazioneita ?denominazioneita } . "
					+ " OPTIONAL{ ?soggetto :nome ?nome }. " + " OPTIONAL{ ?soggetto :cognome ?cognome}  "

					+ " }  ";

			System.out.println(queryString);
			TupleQuery tupleQuery;

			int i = 0;

			tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

			TupleQueryResult qresult = tupleQuery.evaluate();

			while (qresult.hasNext()) {
				BindingSet bindingSet = qresult.next();

				Value urisoggetto = bindingSet.getValue("soggetto");
				Value codicefiscale = bindingSet.getValue("codicefiscale");
				Value nome = bindingSet.getValue("nome");
				Value cognome = bindingSet.getValue("cognome");

				Value tiposoggetto = bindingSet.getValue("tiposoggetto");
				Value denominazioneita = bindingSet.getValue("denominazioneita");

				Soggetto soggetto = new Soggetto();

				if (codicefiscale != null) {
					soggetto.setCodiceFiscale(codicefiscale.stringValue());
				}

				if (tiposoggetto != null) {
					soggetto.setTiposoggetto(tiposoggetto.stringValue());
				}

				if (denominazioneita != null) {
					soggetto.setDenominazioneita(denominazioneita.stringValue());
				}

				if (urisoggetto != null) {
					soggetto.setUri(urisoggetto.stringValue());
				}
				if (nome != null) {
					soggetto.setNome(nome.stringValue());
				}
				if (cognome != null) {
					soggetto.setCognome(cognome.stringValue());
				}

				listaSoggetto.add(soggetto);
			}
			qresult.close();
			connection.close();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}
		// return listaSoggetto;
		return new JSONWithPadding(new GenericEntity<List<Soggetto>>(listaSoggetto) {
		}, callback);

	}

	@GET
	@Path("/utenzeacquaui_old")
	@Produces(MediaType.APPLICATION_JSON)
	public List<UtenzaAcqua> getUtenzeAcquaUI_old(

			@QueryParam("codcomunecatastale") String codcomunecatastale, @QueryParam("particella") String particella,
			@QueryParam("subalterno") String subalterno

	) {

		Soggetto sog = new Soggetto();
		// String springlesserverURL = "http://localhost:8080/openrdf-sesame";

		// String springlesrepositoryID ="georeporter";

		List<BindingSet> tuples = new ArrayList<BindingSet>();
		List<UtenzaAcqua> listaUtenzaAcqua = new ArrayList<UtenzaAcqua>();

		/// Soggetto soggetto =new Soggetto();

		Repository myRepository = new HTTPRepository(springlesserverURL, springlesrepositoryID);
		try {
			myRepository.initialize();

			RepositoryConnection connection = myRepository.getConnection();

			String queryString = queryStringPrefix

					/*
					 * +" select    ?acqua ?datainizio ?datafine ?contribuente ?categoriadescrizione"
					 * +" where{ " +"     ?acqua a :UtenzaAcqua .   "
					 * +"     ?acqua :comunecatastale \""+codcomunecatastale+"\" . "
					 * +"     ?acqua :datainizio ?datainizio. "
					 * +"     ?acqua :categoriadescrizione ?categoriadescrizione. "
					 * +"     ?acqua :contribuente ?contribuente. "
					 * +"     ?acqua :particellaedificabile \""+particella+"\" . "
					 * 
					 * 
					 * +"     ?acqua :subalterno \""+subalterno+"\" " +" }" ;
					 * 
					 */

					+ " select    ?acqua ?datainizio ?datafine ?contribuente ?categoriadescrizione" + " where{ "
					+ "     ?acqua a :UtenzaAcqua .   " + "     ?acqua :comunecatastale \"" + codcomunecatastale
					+ "\" . " + "     ?acqua :datainizio ?datainizio. "
					+ "     ?acqua :categoriadescrizione ?categoriadescrizione. "
					+ "     ?acqua :contribuente ?contribuente. " + "     ?acqua :particellaedificabile \"" + particella
					+ "\" . "

					+ "     ?acqua :subalterno \"" + subalterno + "\" " + " }";

			System.out.println(queryString);
			TupleQuery tupleQuery;

			int i = 0;

			tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

			TupleQueryResult qresult = tupleQuery.evaluate();

			while (qresult.hasNext()) {
				BindingSet bindingSet = qresult.next();

				Value acquaURI = bindingSet.getValue("acqua");
				Value datainizio = bindingSet.getValue("datainizio");
				Value datafine = bindingSet.getValue("datafine");
				Value contribuente = bindingSet.getValue("contribuente");
				Value categoriadescrizione = bindingSet.getValue("categoriadescrizione");

				UtenzaAcqua utenzaacqua = new UtenzaAcqua();

				if (acquaURI != null) {
					utenzaacqua.setUri(acquaURI.stringValue());
				}

				if (datainizio != null) {
					utenzaacqua.setDataInizio(datainizio.stringValue());
				}
				if (datafine != null) {

					utenzaacqua.setDatafine(datafine.stringValue());
				}
				if (contribuente != null) {
					utenzaacqua.setContribuente((contribuente.stringValue()));
				}
				if (categoriadescrizione != null) {
					utenzaacqua.setCategoriaDescrizione((categoriadescrizione.stringValue()));
				}

				listaUtenzaAcqua.add(utenzaacqua);
			}
			// qresult.close();
			// connection.close();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}
		return listaUtenzaAcqua;

	}

	@GET
	@Path("/utenzeacquaui")
	@Produces({ "application/javascript" })
	// @Produces(MediaType.APPLICATION_JSON)

	// public List<UtenzaAcqua> getUtenzeAcquaUI(
	public JSONWithPadding getUtenzeAcquaUI(@QueryParam("callback") String callback, @QueryParam("codui") String codUI
	// @QueryParam("particella") String particella,
	// @QueryParam("subalterno") String subalterno

	) {

		Soggetto sog = new Soggetto();
		// String springlesserverURL = "http://localhost:8080/openrdf-sesame";

		// String springlesrepositoryID ="georeporter";

		List<BindingSet> tuples = new ArrayList<BindingSet>();
		List<UtenzaAcqua> listaUtenzaAcqua = new ArrayList<UtenzaAcqua>();

		/// Soggetto soggetto =new Soggetto();

		Repository myRepository = new HTTPRepository(springlesserverURL, springlesrepositoryID);
		try {
			myRepository.initialize();

			RepositoryConnection connection = myRepository.getConnection();

			String queryString = queryStringPrefix

					/*
					 * +" select    ?acqua ?datainizio ?datafine ?contribuente ?categoriadescrizione"
					 * +" where{ " +"     ?acqua a :UtenzaAcqua .   "
					 * +"     ?acqua :comunecatastale \""+codcomunecatastale+"\" . "
					 * +"     ?acqua :datainizio ?datainizio. "
					 * +"     ?acqua :categoriadescrizione ?categoriadescrizione. "
					 * +"     ?acqua :contribuente ?contribuente. "
					 * +"     ?acqua :particellaedificabile \""+particella+"\" . "
					 * 
					 * 
					 * +"     ?acqua :subalterno \""+subalterno+"\" " +" }" ;
					 * 
					 */

					+ " select    ?acqua ?datainizio ?contribuente ?categoriadescrizione ?notabreve ?codicefiscale ?interno"
					+ " where{ "

					+ "     ?idcat a :IdentificativoCatastale .   " + "     ?idcat :hasUnitaImmobiliare :" + codUI
					+ " ." + "     ?idcat :Subalterno ?subalterno ." + "     ?idcat :Numero ?numero ."

					+ "     ?acqua a :UtenzaAcqua .   "

					+ "     ?acqua :datainizio ?datainizio . "
					+ "     ?acqua :categoriadescrizione ?categoriadescrizione . "
					+ "     ?acqua :contribuente ?contribuente . " + "     ?acqua :particellaedificabile ?numero . "

					+ "   OPTIONAL{   ?acqua :codfiscale ?codicefiscale }. "
					+ "   OPTIONAL{  ?acqua :notabreve ?notabreve }. " + "   OPTIONAL{  ?acqua :interno ?interno} . "
					+ "     ?acqua :subalterno ?subalterno  " + " }";

			System.out.println(queryString);
			TupleQuery tupleQuery;

			int i = 0;

			tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

			TupleQueryResult qresult = tupleQuery.evaluate();

			while (qresult.hasNext()) {
				BindingSet bindingSet = qresult.next();

				Value acquaURI = bindingSet.getValue("acqua");
				Value datainizio = bindingSet.getValue("datainizio");
				Value interno = bindingSet.getValue("interno");
				Value notabreve = bindingSet.getValue("notabreve");
				Value codicefiscale = bindingSet.getValue("codicefiscale");
				Value contribuente = bindingSet.getValue("contribuente");
				Value categoriadescrizione = bindingSet.getValue("categoriadescrizione");

				UtenzaAcqua utenzaacqua = new UtenzaAcqua();

				if (acquaURI != null) {
					utenzaacqua.setUri(acquaURI.stringValue());
				}
				if (datainizio != null) {
					utenzaacqua.setDataInizio(datainizio.stringValue());
				}

				if (interno != null) {
					utenzaacqua.setInterno(interno.stringValue());
				}
				if (notabreve != null) {

					utenzaacqua.setNotabreve((notabreve.stringValue()));
				}
				if (codicefiscale != null) {

					utenzaacqua.setCodicefiscale((codicefiscale.stringValue()));
				}

				if (contribuente != null) {
					utenzaacqua.setContribuente((contribuente.stringValue()));
				}
				if (categoriadescrizione != null) {
					utenzaacqua.setCategoriaDescrizione((categoriadescrizione.stringValue()));
				}

				listaUtenzaAcqua.add(utenzaacqua);
			}
			// qresult.close();
			// connection.close();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}
		// return listaUtenzaAcqua;
		return new JSONWithPadding(new GenericEntity<List<UtenzaAcqua>>(listaUtenzaAcqua) {
		}, callback);
	}

	@GET
	@Path("/iciimuui")
	@Produces({ "application/javascript" })
	// @Produces(MediaType.APPLICATION_JSON)
	// public List<TributiICI> getICI_IMU_UI(

	public JSONWithPadding getICI_IMU_UI(@QueryParam("callback") String callback, @QueryParam("codui") String codUI

	) {

		Soggetto sog = new Soggetto();
		// String springlesserverURL = "http://localhost:8080/openrdf-sesame";

		// String springlesrepositoryID ="georeporter";

		List<BindingSet> tuples = new ArrayList<BindingSet>();
		List<TributiICI> listaTributiICI = new ArrayList<TributiICI>();

		/// Soggetto soggetto =new Soggetto();

		Repository myRepository = new HTTPRepository(springlesserverURL, springlesrepositoryID);
		try {
			myRepository.initialize();

			RepositoryConnection connection = myRepository.getConnection();

			String queryString = queryStringPrefix

					+ " select    ?ici  ?contribuente ?indirizzo ?categoriadescrizione ?rendita" + " where{ "

					+ "     ?idcat a :IdentificativoCatastale .   " + "     ?idcat :hasUnitaImmobiliare :" + codUI
					+ " ." + "     ?idcat :Subalterno ?subalterno ." + "     ?idcat :Numero ?numero ."

					+ "     ?ici a :ICI_IMU .   "
					// +" ?ici :codcomune \""+codcomunecatastale+"\" . "
					+ "     ?ici :indirizzo ?indirizzo. " + "     ?ici :categoriadescrizione ?categoriadescrizione. "
					+ "     ?ici :contribuente ?contribuente. " + "     ?ici :rendita ?rendita. "
					+ "     ?ici :particellaedificabile ?numero . " + "     ?ici :subalterno  ?subalterno  " + " }";

			System.out.println(queryString);
			TupleQuery tupleQuery;

			int i = 0;

			tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

			TupleQueryResult qresult = tupleQuery.evaluate();

			while (qresult.hasNext()) {
				BindingSet bindingSet = qresult.next();

				Value iciURI = bindingSet.getValue("ici");
				Value indirizzo = bindingSet.getValue("indirizzo");
				Value rendita = bindingSet.getValue("rendita");
				Value contribuente = bindingSet.getValue("contribuente");
				Value categoriadescrizione = bindingSet.getValue("categoriadescrizione");

				TributiICI tributiici = new TributiICI();

				if (iciURI != null) {
					tributiici.setUri(iciURI.stringValue());
				}

				if (indirizzo != null) {
					tributiici.setIndirizzo(indirizzo.stringValue());
				}
				if (rendita != null) {
					tributiici.setRendita(rendita.stringValue());
				}
				if (contribuente != null) {
					tributiici.setContribuente((contribuente.stringValue()));
				}
				if (categoriadescrizione != null) {
					tributiici.setCategoriadescrizione((categoriadescrizione.stringValue()));
				}

				listaTributiICI.add(tributiici);
			}
			// qresult.close();
			// connection.close();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}
		return new JSONWithPadding(new GenericEntity<List<TributiICI>>(listaTributiICI) {
		}, callback);
		// return listaTributiICI;

	}

	@POST
	@Path("/inserttable")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String insertTable(

			JSONObject input

	) {

		String result = "FAIL";
		ObjectMapper mapper = new ObjectMapper();
		MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
		Map<String, Object> data;

		// data = mapper.readValue(input, type);

		String iduri;
		String idtabella;
		try {
			iduri = (String) input.get("iduri");
			idtabella = (String) input.get("idtabella");

			Repository myRepository = new HTTPRepository(springlesserverURL, springlesrepositoryID);

			Map<String, String> inputparam_HM = new HashMap<String, String>();
			Map<String, Attributo> tableAttributi_HM = new HashMap<String, Attributo>();
			Map<String, Attributo> insert_HM = new HashMap<String, Attributo>();

			inputparam_HM = jsonToMap(input);

			List<Attributo> tableAttribut_List = getAttributiTabella(idtabella);

			// converto la lista in HM
			for (Attributo attributo : tableAttribut_List) {
				System.out.println(attributo.getNome() + " " + attributo.getTipo() + " " + attributo.getValore());
				tableAttributi_HM.put(attributo.getNome(), attributo);
			}
			// preparo la HM dei parametri per l'inserimento
			for (String key : inputparam_HM.keySet()) {
				// prendo solo quelli che hanno nome giusto
				if (tableAttributi_HM.containsKey(key)) {

					Attributo attr = tableAttributi_HM.get(key);
					attr.setValore(inputparam_HM.get(key));
					insert_HM.put(key, attr);

				}
			}

			myRepository.initialize();
			RepositoryConnection con = myRepository.getConnection();
			String queryString = "";

			ValueFactory factory = myRepository.getValueFactory();
			URI uiuruui = factory.createURI(iduri);
			URI ui = factory.createURI(":" + idtabella);
			URI context = factory.createURI(uricontesto);

			con.begin();
			con.add(uiuruui, RDF.TYPE, ui, context);

			for (String key : insert_HM.keySet()) {
				Attributo attr = insert_HM.get(key);
				// getLiteral mi restituisce il tipo Literal corretto
				Literal lit = getLiteral(attr, factory);
				System.out.println(uiuruui + " " + factory.createURI(key) + " " + lit + " " + context);
				con.add(uiuruui, factory.createURI(key), lit, context);
			}

			con.commit();

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			System.out.println("ERRORE: parametro iduri o idtabella mancante");
			// e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public Literal getLiteral(Attributo attributo, ValueFactory factory) {

		Literal l = factory.createLiteral("");
		String result = "";
		String localName = attributo.getTipo();
		if (localName.equals(Costanti.INTEGER.stringValue())) {
			result = "^^xsd:integer";
			Integer intvalue = (new Integer(attributo.getValore()));
			l = factory.createLiteral(intvalue.intValue());
		}
		if (localName.equals(Costanti.STRING.stringValue())) {
			result = "^^xsd:string";
			l = factory.createLiteral(attributo.getValore());
		}
		if (localName.equals(Costanti.FLOAT.stringValue())) {
			result = "^^xsd:float";
			Float floatvalue = (new Float(attributo.getValore()));
			l = factory.createLiteral(floatvalue.floatValue());
		}
		if (localName.equals(Costanti.DATETIME.stringValue())) {
			result = "^^xsd:dateTime";
			Date datevalue = (new Date(attributo.getValore()));
			l = factory.createLiteral(datevalue);
		}
		if (localName.equals(Costanti.BOOLEAN.stringValue())) {
			result = "^^xsd:boolean";
			Boolean boolvalue = (new Boolean(attributo.getValore()));
			l = factory.createLiteral(boolvalue.booleanValue());
		}
		return l;

	}

	@GET
	@Path("/utenzerifiutiintestatarioui")
	@Produces(MediaType.APPLICATION_JSON)
	public List<UtenzaRifiuti> getUtenzeRifiuti() {

		List<UtenzaRifiuti> listaUtenzaRifiuti = new ArrayList<UtenzaRifiuti>();

		return listaUtenzaRifiuti;

	}

	@GET
	@Path("/tributiiciintestatarioui")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TributiICI> getTributi() {

		List<TributiICI> listaTributiICI = new ArrayList<TributiICI>();

		return listaTributiICI;

	}

	@GET
	@Path("/fornituregasintestatarioui")
	@Produces(MediaType.APPLICATION_JSON)
	public List<FornituraGas> getFornitureGasIntestatarioui() {

		List<FornituraGas> listaFornituraGas = new ArrayList<FornituraGas>();

		return listaFornituraGas;

	}

	@GET
	@Path("/fornitureenergiaintestatarioui")
	@Produces(MediaType.APPLICATION_JSON)
	public List<FornituraEnergia> getFornitureEnergiaIntestatarioui() {

		List<FornituraEnergia> listaFornituraEnergia = new ArrayList<FornituraEnergia>();

		return listaFornituraEnergia;

	}

	@GET
	@Path("/forniturelocazioniintestatarioui")
	@Produces(MediaType.APPLICATION_JSON)
	public List<FornituraLocazioni> getFornitureLocazioniIntestatarioui() {

		List<FornituraLocazioni> listaFornituraLocazioni = new ArrayList<FornituraLocazioni>();

		return listaFornituraLocazioni;

	}

	@POST
	@Path("/unitaimmobiliare")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String insertUnitaimmobiliare(

			JSONObject input

	) {

		String result = "FAIL";
		ObjectMapper mapper = new ObjectMapper();
		MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
		Map<String, Object> data;

		// data = mapper.readValue(input, type);

		String uiuri;
		try {
			uiuri = (String) input.get("uiuri");

			Repository myRepository = new HTTPRepository(springlesserverURL, springlesrepositoryID);
			if ((uiuri == null) || uiuri.equals("")) {

				ResponseBuilderImpl builder = new ResponseBuilderImpl();
				builder.status(Response.Status.BAD_REQUEST);
				builder.entity("ERROR! uiuri parameter missing or null");
				Response response = builder.build();
				throw new WebApplicationException(response);
			}
			result = uiuri;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	@POST
	@Path("/indentificativocatastale")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String insertIdentificativiCatastale(

			JSONObject input

	) {

		String result = "FAIL";
		ObjectMapper mapper = new ObjectMapper();
		MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
		Map<String, Object> data;

		// data = mapper.readValue(input, type);

		String uiuri;
		try {
			uiuri = (String) input.get("uri");

			// Repository myRepository = new HTTPRepository(springlesserverURL,
			// springlesrepositoryID);
			if ((uiuri == null) || uiuri.equals("")) {

				ResponseBuilderImpl builder = new ResponseBuilderImpl();
				builder.status(Response.Status.BAD_REQUEST);
				builder.entity("ERROR! uiuri parameter missing or null");
				Response response = builder.build();
				throw new WebApplicationException(response);
			}
			result = uiuri;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	@POST
	@Path("/particella")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String insertParticella(

			JSONObject input

	) {

		String result = "FAIL";
		ObjectMapper mapper = new ObjectMapper();
		MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
		Map<String, Object> data;

		// data = mapper.readValue(input, type);

		String uiuri;
		try {
			uiuri = (String) input.get("uri");

			// Repository myRepository = new HTTPRepository(springlesserverURL,
			// springlesrepositoryID);
			if ((uiuri == null) || uiuri.equals("")) {

				ResponseBuilderImpl builder = new ResponseBuilderImpl();
				builder.status(Response.Status.BAD_REQUEST);
				builder.entity("ERROR! uiuri parameter missing or null");
				Response response = builder.build();
				throw new WebApplicationException(response);
			}
			result = uiuri;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	@POST
	@Path("/indirizzo")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String insertIndirizzo(

			JSONObject input

	) {

		String result = "FAIL";
		ObjectMapper mapper = new ObjectMapper();
		MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
		Map<String, Object> data;

		// data = mapper.readValue(input, type);

		String uiuri;
		try {
			uiuri = (String) input.get("uri");

			// Repository myRepository = new HTTPRepository(springlesserverURL,
			// springlesrepositoryID);
			if ((uiuri == null) || uiuri.equals("")) {

				ResponseBuilderImpl builder = new ResponseBuilderImpl();
				builder.status(Response.Status.BAD_REQUEST);
				builder.entity("ERROR! uiuri parameter missing or null");
				Response response = builder.build();
				throw new WebApplicationException(response);
			}
			result = uiuri;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	@GET
	@Path("/inserttopic")
	@Produces(MediaType.APPLICATION_JSON)
	public String insertTopic(@QueryParam("topicDescr") String topicDescr,
			@QueryParam("URIParentTopic") String URIParentTopic

	) {

		// URIParentTopic= checkNameSpace(URIParentTopic);
		String topicURI = ":topic_" + topicDescr.toLowerCase().trim().replace(" ", "_").replaceAll("[^a-zA-Z0-9_]", "");
		String termURI = topicURI + "_t";

		// String rewURI= POIURI+ UUID.randomUUID();

		String result = "FAIL";
		// String delimiter = "\\,";

		Repository myRepository = new HTTPRepository(springlesserverURL, "toolisse");
		try {
			myRepository.initialize();
			RepositoryConnection con = myRepository.getConnection();

			// result= result+""+con.size();
			try {

				String queryString = "";

				// ObjectMapper mapper = new ObjectMapper();
				// try {
				// Kbelement kbelement= mapper.readValue(rdfPayload,Kbelement.class);
				con.begin();

				String queryStringPrefix2 = "PREFIX :<http://dkm.fbk.eu/trentour#>\n"
						+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"
						+ "PREFIX owl2xml:<http://www.w3.org/2006/12/owl2-xml#>\n"
						+ "PREFIX trentour:<http://dkm.fbk.eu/trentour#>\n"
						+ "PREFIX test:<http://dkm.fbk.eu/trentour/test#>\n"
						+ "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"
						+ "PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n"
						+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
						+ "PREFIX ckr: <http://dkm.fbk.eu/ckr/meta#>\n" + "PREFIX meta:<http://dkm.fbk.eu/ckr/meta#>\n";
				queryString = queryStringPrefix2 + "	 INSERT DATA {" + "  GRAPH meta:global {" + "   " + topicURI
						+ " a :Topic ;" + "    :hasTerm \"" + termURI + "\";" + "   :hasParentTopic \"" + URIParentTopic
						+ "\" ." + "   " + termURI + " rdf:type :Term;" + "	:hasText 	\"" + topicDescr + "\" .}"
						+ "}							";

				System.out.println(queryString);

				int i = 0;
				// conn.prepareUpdate(QueryLanguage.SPARQL, updateQuery);
				Update insert = con.prepareUpdate(QueryLanguage.SPARQL, queryString);
				insert.getIncludeInferred();
				insert.execute();
				con.commit();
				con.close();
				result = ("{\"topicURI\":\"" + termURI + "\"}");
				// tupleQuery.setIncludeInferred(true);
				// TupleQueryResult qresult = tupleQuery.evaluate();
				try {

				} finally {
					// qresult.close();

				}

			} finally {
				con.close();

			}

		} catch (OpenRDFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;

	}

	@GET
	@Path("/insertpoi")
	@Produces(MediaType.APPLICATION_JSON)
	public String insertPOI(

			@QueryParam("address") String hasAddress, // R//
			@QueryParam("contactName") String hasContactName, @QueryParam("phoneNumber") String hasPhoneNumber,
			@QueryParam("faxNumber") String hasFaxNumber, @QueryParam("mailAddress") String hasMailAddress,
			@QueryParam("ticketCost") String hasTicketCost, @QueryParam("openingHours") String hasOpeningHours,
			@QueryParam("accessibilityInfo") String hasAccessibilityInfo, @QueryParam("capacity") String hasCapacity,
			@QueryParam("status") String hasStatus, @QueryParam("image") String hasImage,
			@QueryParam("video") String hasVideo, @QueryParam("sourceID") String hasSourceID,
			@QueryParam("timeZone") String hasTimeZone, @QueryParam("sourceURL") String hasSourceURL,
			@QueryParam("externalAuthorName") String hasExternalAuthorName,
			@QueryParam("externalAuthorPic") String hasExternalAuthorPic,
			@QueryParam("externalAuthorLink") String hasExternalAuthorLink, @QueryParam("ranking") String hasRanking,
			@QueryParam("parentPOI") String hasParentPOI, @QueryParam("review") String hasReview,

			// Location attributes

			@QueryParam("longitude") String hasLongitude, // R//
			@QueryParam("latitude") String hasLatitude, // R//
			@QueryParam("altitude") String hasAltitude, @QueryParam("radius") String hasRadius,
			@QueryParam("parentLocation") String hasParentlocation,
			// KnowledgeElement attributes

			@QueryParam("description") String hasDescription, // R//
			@QueryParam("externalPage") String hasExternalPage,
			@QueryParam("wikipediaArticle") String hasWikipediaArticle, @QueryParam("term") String hasTerm,

			@QueryParam("POICategory") String hasPOICategory
	// @QueryParam("reviewURL") String hasReviewURL,
	// @QueryParam("reviewText") String hasReviewText
	) {

		if ((hasTerm == null) || hasTerm.equals("")) {

			ResponseBuilderImpl builder = new ResponseBuilderImpl();
			builder.status(Response.Status.BAD_REQUEST);
			builder.entity("ERROR! term parameter missing or null");
			Response response = builder.build();
			throw new WebApplicationException(response);
		}

		/*
		 * if ((hasAddress==null)||hasAddress.equals("")){
		 * 
		 * ResponseBuilderImpl builder = new ResponseBuilderImpl();
		 * builder.status(Response.Status.BAD_REQUEST);
		 * builder.entity("ERROR! POI Address parameter missing or null"); Response
		 * response = builder.build(); throw new WebApplicationException(response); }
		 */

		if ((hasLatitude == null) || hasLongitude.equals("")) {

			ResponseBuilderImpl builder = new ResponseBuilderImpl();
			builder.status(Response.Status.BAD_REQUEST);
			builder.entity("ERROR! POI Latitude parameter missing or null");
			Response response = builder.build();
			throw new WebApplicationException(response);
		}
		if ((hasLongitude == null) || hasLongitude.equals("")) {

			ResponseBuilderImpl builder = new ResponseBuilderImpl();
			builder.status(Response.Status.BAD_REQUEST);
			builder.entity("ERROR! POI Logitude parameter missing or null");
			Response response = builder.build();
			throw new WebApplicationException(response);
		}
		if ((hasParentlocation != null) && !hasParentlocation.equals("")) {
			// hasParentlocation=findKBLocationURI(hasParentlocation).getId();
		}
		// checkNameSpace("parentLocation",parentLocation);
		// checkNameSpace("poiCategory",poicategory);

		String hasAddress_string = "";
		String hasContactName_string = "";
		String hasPhoneNumber_string = "";
		String hasFaxNumber_string = "";
		String hasMailAddress_string = "";
		String hasTicketCost_string = "";
		String hasOpeningHours_string = "";
		String hasAccessibilityInfo_string = "";
		String hasCapacity_string = "";
		String hasStatus_string = "";
		String hasImage_string = "";
		String hasVideo_string = "";
		String hasSourceID_string = "";
		String hasTimeZone_string = "";
		String hasSourceURL_string = "";

		String hasExternalAuthorName_string = "";
		String hasExternalAuthorPic_string = "";
		String hasExternalAuthorLink_string = "";
		String hasRanking_string = "";
		String hasParentPOI_string = "";
		String hasReview_string = "";

		// Location attributes

		String hasLongitude_string = "";// R//
		String hasLatitude_string = "";// R//
		String hasAltitude_string = "";
		String hasRadius_string = "";
		String hasParentLocation_string = "";
		// KnowledgeElement attributes

		String hasDescription_string = "";// R//
		String hasExternalPage_string = "";
		String hasWikipediaArticle_string = "";

		String hasPOICategory_string = "";

		if (!(hasAddress == null) && (!hasAddress.equals(""))) {
			hasAddress_string = "	 :hasAddress \"" + hasAddress + "\" ; ";
		}

		if (!(hasContactName == null) && (!hasContactName.equals(""))) {
			hasContactName_string = "	 :hasContactName \"" + hasContactName + "\" ; ";
		}
		if (!(hasPhoneNumber == null) && (!hasPhoneNumber.equals(""))) {
			hasPhoneNumber_string = "	 :hasPhoneNumber \"" + hasPhoneNumber + "\" ; ";
		}
		if (!(hasFaxNumber == null) && (!hasFaxNumber.equals(""))) {
			hasFaxNumber_string = "	 :hasFaxNumber \"" + hasFaxNumber + "\" ; ";
		}
		if (!(hasMailAddress == null) && (!hasMailAddress.equals(""))) {
			hasMailAddress_string = "	 :hasMailAddress \"" + hasMailAddress + "\" ; ";
		}

		if (!(hasTicketCost == null) && (!hasTicketCost.equals(""))) {
			hasTicketCost_string = "	 :hasTicketCost \"" + hasTicketCost + "\" ; ";
		}

		if (!(hasOpeningHours == null) && (!hasOpeningHours.equals(""))) {
			hasOpeningHours_string = "	 :hasOpeningHours \"" + hasOpeningHours + "\" ; ";
		}
		if (!(hasAccessibilityInfo == null) && (!hasAccessibilityInfo.equals(""))) {
			hasAccessibilityInfo_string = "	 :hasAccessibilityInfo \"" + hasAccessibilityInfo + "\" ; ";
		}
		if (!(hasCapacity == null) && (!hasCapacity.equals(""))) {
			hasCapacity_string = "	 :hasCapacity \"" + hasCapacity + "\" ; ";
		}
		if (!(hasStatus == null) && (!hasStatus.equals(""))) {
			hasStatus_string = "	 :hasStatus \"" + hasStatus + "\" ; ";
		}
		if (!(hasImage == null) && (!hasImage.equals(""))) {
			hasImage_string = "	 :hasImage \"" + hasImage + "\" ; ";
		}
		if (!(hasVideo == null) && (!hasVideo.equals(""))) {
			hasVideo_string = "	 :hasVideo \"" + hasVideo + "\" ; ";
		}
		if (!(hasSourceID == null) && (!hasSourceID.equals(""))) {
			hasSourceID_string = "	 :hasSourceID \"" + hasSourceID + "\" ; ";
		}
		if (!(hasTimeZone == null) && (!hasTimeZone.equals(""))) {
			hasTimeZone_string = "	 :hasTimeZone \"" + hasTimeZone + "\" ; ";
		}
		if (!(hasSourceURL == null) && (!hasSourceURL.equals(""))) {
			hasSourceURL_string = "	 :hasSourceURL \"" + hasSourceURL + "\" ; ";
		}

		if (!(hasExternalAuthorName == null) && (!hasExternalAuthorName.equals(""))) {
			hasExternalAuthorName_string = "	 :hasExternalAuthorName \"" + hasExternalAuthorName + "\" ; ";
		}
		if (!(hasExternalAuthorPic == null) && (!hasExternalAuthorPic.equals(""))) {
			hasExternalAuthorPic_string = "	 :hasExternalAuthorPic \"" + hasExternalAuthorPic + "\" ; ";
		}
		if (!(hasExternalAuthorLink == null) && (!hasExternalAuthorLink.equals(""))) {
			hasExternalAuthorLink_string = "	 :hasExternalAuthorLink \"" + hasExternalAuthorLink + "\" ; ";
		}
		if (!(hasRanking == null) && (!hasRanking.equals(""))) {
			hasRanking_string = "	 :hasRanking \"" + hasRanking + "\"^^xsd:int ; ";
		}
		if (!(hasReview == null) && (!hasReview.equals(""))) {
			hasReview_string = "	 :hasReview \"" + hasReview + "\" ; ";
		}

		if (!(hasParentPOI == null) && (!hasParentPOI.equals(""))) {
			hasParentPOI_string = "	 :hasParentPOI " + hasParentPOI + " ; ";
		}
		//////////////////
		if (!(hasLongitude == null) && (!hasLongitude.equals(""))) {
			hasLongitude_string = "	 :hasLongitude \"" + hasLongitude + "\" ;";
		}
		if (!(hasLatitude == null) && (!hasLatitude.equals(""))) {
			hasLatitude_string = "	 :hasLatitude \"" + hasLatitude + "\" ;";
		}
		if (!(hasAltitude == null) && (!hasAltitude.equals(""))) {
			hasAltitude_string = "	 :hasAltitude \"" + hasAltitude + "\" ;";
		}
		if (!(hasRadius == null) && (!hasRadius.equals(""))) {
			hasRadius_string = "	 :hasRadius \"" + hasRadius + "\" ;";
		}

		if (!(hasParentlocation == null) && (!hasParentlocation.equals(""))) {
			hasParentLocation_string = "	 :hasParentLocation :" + hasParentlocation + " ;";
		}

		//////////
		if (!(hasExternalPage == null) && (!hasExternalPage.equals(""))) {
			hasExternalPage_string = "	 :hasExternalPage \"" + hasExternalPage + "\" ; ";
		}

		if (!(hasDescription == null) && (!hasDescription.equals(""))) {
			hasDescription_string = "	 :hasDescription \"" + hasDescription + "\" ;";
		}

		if (!(hasWikipediaArticle == null) && (!hasWikipediaArticle.equals(""))) {
			hasWikipediaArticle_string = "	 :hasWikipediaArticle \"" + hasWikipediaArticle + "\" ;";
		}

		if (!(hasPOICategory == null) && (!hasPOICategory.equals(""))) {
			hasPOICategory_string = "	 :hasPOICategory " + ":cat_" + hasPOICategory + " ;";
		}

		String result = "{\"Status\":\"FAIL\"}";
		// String delimiter = "\\,";
		String name_norm = "poi_" + hasTerm.toLowerCase().trim().replace(" ", "_").replaceAll("[^a-zA-Z0-9_]", "");
		// name_norm=name_norm.replace(" ", "_");
		String term = name_norm + "_t";
		// String[] keywords_array;
		// keywords_array = keywords.split(delimiter);
		Repository myRepository = new HTTPRepository(springlesserverURL, "toolisse");
		try {
			myRepository.initialize();
			RepositoryConnection con = myRepository.getConnection();

			// result= result+""+con.size();
			try {

				String queryString = "";

				ObjectMapper mapper = new ObjectMapper();
				// try {
				// Kbelement kbelement= mapper.readValue(rdfPayload,Kbelement.class);

				String queryStringPrefix2 = "PREFIX :<http://dkm.fbk.eu/trentour#>\n"
						+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"
						+ "PREFIX owl2xml:<http://www.w3.org/2006/12/owl2-xml#>\n"
						+ "PREFIX trentour:<http://dkm.fbk.eu/trentour#>\n"
						+ "PREFIX test:<http://dkm.fbk.eu/trentour/test#>\n"
						+ "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"
						+ "PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n"
						+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
						+ "PREFIX ckr: <http://dkm.fbk.eu/ckr/meta#>\n" + "PREFIX meta:<http://dkm.fbk.eu/ckr/meta#>\n";
				queryString = queryStringPrefix2

						+ " INSERT DATA { " + "	 GRAPH meta:global { :" + name_norm + " a :POI ;" + hasAddress_string
						+ hasContactName_string + hasPhoneNumber_string + hasFaxNumber_string + hasMailAddress_string
						+ hasTicketCost_string + hasOpeningHours_string + hasAccessibilityInfo_string
						+ hasCapacity_string + hasStatus_string + hasImage_string + hasVideo_string + hasSourceID_string
						+ hasTimeZone_string + hasSourceURL_string + hasExternalAuthorName_string
						+ hasExternalAuthorPic_string + hasExternalAuthorLink_string + hasRanking_string
						+ hasReview_string + hasParentPOI_string

						// Location attributes

						+ hasLongitude_string// R//
						+ hasLatitude_string// R//
						+ hasAltitude_string + hasRadius_string + hasParentLocation_string
						// KnowledgeElement attributes

						+ hasDescription_string// R//
						+ hasExternalPage_string + hasWikipediaArticle_string + hasPOICategory_string

						+ "	 :hasTerm :" + term + " ." + "	 :" + term + " a :Term ;" + "	  :hasText " + "	 \""
						+ hasTerm + "\"@it .}" + "	 }";
				con.begin();

				System.out.println(queryString);

				int i = 0;
				// conn.prepareUpdate(QueryLanguage.SPARQL, updateQuery);
				Update insert = con.prepareUpdate(QueryLanguage.SPARQL, queryString);
				insert.execute();
				con.commit();
				con.close();
				result = "{\"POIURI\":\"" + name_norm + "\"}";
				// tupleQuery.setIncludeInferred(true);
				// TupleQueryResult qresult = tupleQuery.evaluate();
				try {

				} finally {
					// qresult.close();

				}

			} finally {
				con.close();

			}

		} catch (OpenRDFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}
	
	
	@GET
	@Path("/mappingtabella")
	@Produces(MediaType.APPLICATION_JSON)
	public MappingTabella getMappingTabella(

			@QueryParam("tabella") String tabella
	// @QueryParam("springlesrepositoryID") String springlesrepositoryID
	) {

		// String springlesrepositoryID ="georeporter";
        MappingTabella mappingtabella =new MappingTabella();
 		List<BindingSet> tuples = new ArrayList<BindingSet>();
		List<Attributo> listaAttributi = new ArrayList<Attributo>();

		Repository myRepository = new HTTPRepository(springlesserverURL, springlesrepositoryID);
		try {
			myRepository.initialize();

			RepositoryConnection connection = myRepository.getConnection();

			String queryString = queryStringPrefix

					+ "select ?property ?range where { "
					+ "  ?property rdfs:domain/(owl:unionOf/rdf:rest*/rdf:first)* :" + tabella + " ."
					+ " ?property rdfs:range ?range " + "}";

			System.out.println(queryString);
			TupleQuery tupleQuery;

			int i = 0;

			tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

			TupleQueryResult qresult = tupleQuery.evaluate();

			while (qresult.hasNext()) {
				BindingSet bindingSet = qresult.next();

				Value property = bindingSet.getValue("property");
				Value range = bindingSet.getValue("range");

				Attributo attributo = new Attributo();

				if (property != null) {
					attributo.setNome(property.stringValue());
					attributo.setMapping(property.stringValue());
				}

				if (range != null) {
					attributo.setTipo(range.stringValue());
				}

				listaAttributi.add(attributo);
			}
			Attributo nome =new Attributo();
			nome.setNome(tabella);
			nome.setMapping(tabella);
			mappingtabella.setId(nome);
			mappingtabella.setAttributi(listaAttributi);
			qresult.close();
			connection.close();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}

		return mappingtabella;
	}
	
	
	
	@GET
	@Path("/attributitabella")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Attributo> getAttributiTabella(

			@QueryParam("tabella") String tabella
	// @QueryParam("springlesrepositoryID") String springlesrepositoryID
	) {

		// String springlesrepositoryID ="georeporter";

		List<BindingSet> tuples = new ArrayList<BindingSet>();
		List<Attributo> listaAttributi = new ArrayList<Attributo>();

		Repository myRepository = new HTTPRepository(springlesserverURL, springlesrepositoryID);
		try {
			myRepository.initialize();

			RepositoryConnection connection = myRepository.getConnection();

			String queryString = queryStringPrefix

					+ "select ?property ?range where { "
					+ "  ?property rdfs:domain/(owl:unionOf/rdf:rest*/rdf:first)* :" + tabella + " ."
					+ " ?property rdfs:range ?range " + "}";

			System.out.println(queryString);
			TupleQuery tupleQuery;

			int i = 0;

			tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

			TupleQueryResult qresult = tupleQuery.evaluate();

			while (qresult.hasNext()) {
				BindingSet bindingSet = qresult.next();

				Value property = bindingSet.getValue("property");
				Value range = bindingSet.getValue("range");

				Attributo attributo = new Attributo();

				if (property != null) {
					attributo.setNome(property.stringValue());
				}

				if (range != null) {
					attributo.setTipo(range.stringValue());
				}

				listaAttributi.add(attributo);
			}
			qresult.close();
			connection.close();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}

		return listaAttributi;
	}

	public static HashMap<String, String> jsonToMap(JSONObject jObject) throws JSONException {

		HashMap<String, String> map = new HashMap<String, String>();

		Iterator<?> keys = jObject.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();
			String value = jObject.getString(key);
			map.put(key, value);

		}

		System.out.println("json : " + jObject);
		System.out.println("map : " + map);
		return map;
	}
}
