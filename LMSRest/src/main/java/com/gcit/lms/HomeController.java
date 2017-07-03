package com.gcit.lms;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gcit.lms.dao.AuthorDAO;
import com.gcit.lms.dao.BookCopyDAO;
import com.gcit.lms.dao.BookDAO;
import com.gcit.lms.dao.BookLoanDAO;
import com.gcit.lms.dao.BorrowerDAO;
import com.gcit.lms.dao.BranchDAO;
import com.gcit.lms.dao.GenreDAO;
import com.gcit.lms.dao.PublisherDAO;
import com.gcit.lms.entity.Author;
import com.gcit.lms.entity.Book;
import com.gcit.lms.entity.BookCopy;
import com.gcit.lms.entity.BookLoan;
import com.gcit.lms.entity.Borrower;
import com.gcit.lms.entity.Branch;
import com.gcit.lms.entity.Publisher;
import com.gcit.lms.service.AdminService;
import com.gcit.lms.service.BorrowerService;
import com.gcit.lms.service.LibrarianService;

@RestController
public class HomeController {
	
	@Autowired
	AdminService adminService;
	
	@Autowired
	LibrarianService librarianService;
	
	@Autowired
	BorrowerService borrowerService;
	
	@Autowired
	AuthorDAO adao;

	@Autowired
	BookDAO bdao;

	@Autowired
	GenreDAO gdao;

	@Autowired
	PublisherDAO pdao;

	@Autowired
	BranchDAO brdao;

	@Autowired
	BorrowerDAO bodao;

	@Autowired
	BookLoanDAO bldao;
	
	@Autowired
	BookCopyDAO bcdao;
	
	//================================================================================
    // Welcome page && Home menu
    //================================================================================
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		return "welcome";
	}
	
	@RequestMapping(value = "/admin", method = RequestMethod.GET)
	public String admin() {
		return "admin";
	}
	
	@RequestMapping(value = "/librarian", method = RequestMethod.GET)
	public String librarian(Model model) throws SQLException {
		model.addAttribute("branches", librarianService.getAllBranches());
		return "librarian";
	}
	
	@RequestMapping(value = "/borrower", method = RequestMethod.GET)
	public String borrower() {
		return "borrower";
	}
	
	//================================================================================
    // LIBRARIAN SERVICES
    //================================================================================
	
	@RequestMapping(value = "/l_editbranch", method = RequestMethod.GET)
	public String l_editBranch(Model model, 
			@RequestParam("branchId") Integer branchId) throws SQLException {
		Branch branch = adminService.getBranchByPK(branchId);
		model.addAttribute("branch", branch);
		return "l_editbranch";
	}
	
	@RequestMapping(value = "/editBranchLib", method = RequestMethod.POST)
	public String editBranchLib(Model model, 
			@RequestParam("branchId") Integer branchId,
			@RequestParam("branchName") String branchName, 
			@RequestParam(value = "branchAddress", required = false) String branchAddress) throws SQLException {
		
		Branch branch = adminService.getBranchByPK(branchId);
		branch.setBranchName(branchName);
		if (branchAddress != null && branchAddress.length() > 0){
			branch.setBranchAddress(branchAddress);
		}
		adminService.saveBranch(branch);
		return librarian(model);
	}
	
	
	@RequestMapping(value = "/getBookCopies", method = RequestMethod.GET)
	public String getBookCopies(Model model, 
			@RequestParam("branchId") Integer branchId) throws SQLException {
		Branch branch = librarianService.getBranchByPk(branchId);
		model.addAttribute("branchId", branchId);
		model.addAttribute("branch", branch);
		return l_viewBookCopies(model, branchId);
	}
	
	@RequestMapping(value = "/l_viewbookcopies", method = RequestMethod.GET)
	public String l_viewBookCopies(Model model, 
			@RequestParam("branchId") Integer branchId) throws SQLException {
		Branch branch = librarianService.getBranchByPk(branchId);
		model.addAttribute("branch", branch);
		List<BookCopy> copies = librarianService.getAllBookCopiesOwnedBy(branch);
		model.addAttribute("copies", copies);
		return "l_viewbookcopies";
	}
	
	@RequestMapping(value = "/l_editbookcopy", method = RequestMethod.GET)
	public String l_editbookcopy(Model model, 
			@RequestParam("branchId") Integer branchId, 
			@RequestParam("bookId") Integer bookId,
			@RequestParam("noOfCopies") Integer noOfCopies) throws SQLException {
		model.addAttribute("branchId", branchId);
		model.addAttribute("bookId", bookId);
		model.addAttribute("oldNoOfCopies", noOfCopies);
		return "l_editbookcopy";
	}
	
	@RequestMapping(value = "/editBookCopy", method = RequestMethod.POST)
	public String editBookCopy(Model model, @RequestParam("branchId") Integer branchId, @RequestParam("bookId") Integer bookId,
			@RequestParam("noOfCopies") Integer noOfCopies) throws SQLException {
		BookCopy copy = librarianService.getBookCopyByPks(branchId, bookId);
		copy.setNoOfCopies(noOfCopies);
		librarianService.saveBookCopy(copy);
		return l_viewBookCopies(model, branchId);
	}
	
	@RequestMapping(value = "/deleteBookCopy", method = RequestMethod.GET)
	public String deleteBookCopy(Model model, 
			@RequestParam("branchId") Integer branchId, 
			@RequestParam("bookId") Integer bookId) throws SQLException {
		
		librarianService.deleteBookCopy( bookId, branchId);
		String message  = "Deletion completed successfully";
		model.addAttribute("message", message);
		return l_viewBookCopies(model, branchId);
	}
	
	//================================================================================
    // BORROWER SERVICES
    //================================================================================
	
	@RequestMapping(value = "/borrowerLogin", method = RequestMethod.POST)
	public String borrowerLogin(Model model, @RequestParam("cardNo") Integer cardNo) throws SQLException {
		Boolean cardIsValid  = borrowerService.isValidCardNo(cardNo);
		String message = "Please enter a valid card number!";
		if (cardIsValid){
			Borrower borrower = adminService.getBorrowerByPK(cardNo); 
			message = "Welcome "+borrower.getName() + "!";
			model.addAttribute("cardNo",cardNo);
			model.addAttribute("message",message);
			return "b_pickaction";
		}else{
			model.addAttribute("message",cardNo);
			model.addAttribute("message",message);
			return "borrower";
		}
	}
	
	@RequestMapping(value = "/b_pickaction", method = RequestMethod.GET)
	public String b_pickAction(Model model,  @RequestParam("cardNo") Integer cardNo) throws SQLException {
		model.addAttribute("cardNo", cardNo);
		return "b_pickaction";
	}
	
	@RequestMapping(value = "/b_pickbranch", method = RequestMethod.GET)
	public String b_pickBranch(Model model, @RequestParam("cardNo") Integer cardNo) throws SQLException {
		model.addAttribute("branches", adminService.getAllBranches(1, null));
		//Integer branchesCount = adminService.getBranchesCount("");
		Integer pages = 0;// getPagesNumber(branchesCount);
		model.addAttribute("cardNo", cardNo);
		model.addAttribute("pages", pages);
		return "b_pickbranch";
	}
	
	@RequestMapping(value = "/b_viewbooksavailable", method = RequestMethod.GET)
	public String b_viewBooksAvailable(Model model, @RequestParam("cardNo") Integer cardNo,
			@RequestParam("branchId") Integer branchId) throws SQLException { 
		
		Branch branch = librarianService.getBranchByPk(branchId);
		List<BookCopy> copies = librarianService.getAllBookCopiesOwnedBy(branch);
		model.addAttribute("branch", branch);
		model.addAttribute("cardNo", cardNo);
		model.addAttribute("branchId", branchId);
		model.addAttribute("copies", copies);
		return "b_viewbooksavailable";
	}
	
	@RequestMapping(value = "/checkOutBook", method = RequestMethod.GET)
	public String checkOutBook(Model model,@RequestParam("cardNo") Integer cardNo,
			@RequestParam("branchId") Integer branchId, @RequestParam("bookId") Integer bookId) throws SQLException {
		Borrower borrower = borrowerService.getBorrowerByPK(cardNo);
		borrowerService.checkOutBook(bookId, branchId, cardNo);
		String message = borrower.getName()+". Book borrowed successfully";
		model.addAttribute("message", message);
		model.addAttribute("cardNo", cardNo);
		return "b_pickaction";
	}
	
	@RequestMapping(value = "/returnBook", method = RequestMethod.GET)
	public String returnBook(Model model,@RequestParam("cardNo") Integer cardNo,
			@RequestParam("branchId") Integer branchId, 
			@RequestParam("bookId") Integer bookId,
			@RequestParam("dateOut") String dateOut) throws SQLException {
		
		BookLoan bookLoan = new BookLoan();
		dateOut = dateOut.replaceAll("T", " ");
		
		bookLoan.setBook(adminService.getBookByPK(bookId));
		bookLoan.setBorrower(adminService.getBorrowerByPK(cardNo));
		bookLoan.setBranch(adminService.getBranchByPK(branchId));
		bookLoan.setDateOut(dateOut);
		borrowerService.returnBook(bookLoan);
		
		Borrower borrower = borrowerService.getBorrowerByPK(cardNo);
		String message = "Thank you "+borrower.getName()+"! Book returned successfully!";
		model.addAttribute("message", message);
		return b_viewbookloans(model,cardNo);
	}
	
	@RequestMapping(value = "/b_viewbookloans", method = RequestMethod.GET)
	public String b_viewbookloans(Model model, 
			@RequestParam("cardNo") Integer cardNo) throws SQLException {
		
		List<BookLoan> bookloans = adminService.getAllDueBookLoans(1, cardNo); 
		Borrower borrower = adminService.getBorrowerByPK(cardNo);
		
	    //Integer bookloansCount = adminService.getDueBookLoansCount(cardNo);
	    Integer pages = 0; //getPagesNumber(bookloansCount);
		
		model.addAttribute("cardNo",cardNo);
		model.addAttribute("pages",pages);
		model.addAttribute("bookloans",bookloans);
		model.addAttribute("borrower",borrower);
		return "b_viewbookloans";
	}
	
	//================================================================================
    // BELOW HERE IS ADMIN TERRITORY
    //================================================================================

	//================================================================================
    // Books pages
    //================================================================================
	
//	@RequestMapping(value = "/a_book", method = RequestMethod.GET)
//	public String a_book() {
//		return "a_book";
//	}
//	
	
	@RequestMapping(value = "addBook", method = RequestMethod.POST, consumes="application/json")
	public String addBook(@RequestBody Book book) throws SQLException {
		bdao.addBook(book);
		return "Book Added - Success is in the AIR!";
	}
	
	@RequestMapping(value = "editBook", method = RequestMethod.POST, 
			consumes="application/json", produces="application/json")
	public List<Book> editBook(@RequestBody Book book) throws SQLException {
		bdao.updateBook(book);
		return bdao.readAllBooks();
	}
	
	@RequestMapping(value = "/a_viewbooks", method = RequestMethod.GET, produces="application/json")
	public List<Book> a_viewBooks() throws SQLException { 
		List<Book> books =  bdao.readAllBooks();
		for (Book b: books){
			b.setAuthors(adao.readAllAuthorsByBookId(b.getBookId()));
			b.setGenres(gdao.readAllGenresByBookId(b.getBookId()));
			b.setPublisher(pdao.readPublisherByBookId(b.getBookId()));
		}
		return books;
	}
	
	@RequestMapping(value = "/a_viewbooks/{pageNo}", method = RequestMethod.GET, produces="application/json")
	public List<Book> a_viewBooks(@PathVariable Integer pageNo) throws SQLException { 
		List<Book> books =  bdao.readAllBooks(pageNo);
		for (Book b: books){
			b.setAuthors(adao.readAllAuthorsByBookId(b.getBookId()));
			b.setGenres(gdao.readAllGenresByBookId(b.getBookId()));
			b.setPublisher(pdao.readPublisherByBookId(b.getBookId()));
		}
		return books;
	}
	
	@RequestMapping(value = "/a_viewbooks/{pageNo}/{searchString}", method = RequestMethod.GET, produces="application/json")
	public List<Book> a_viewBooks(@PathVariable Integer pageNo, 
			@PathVariable String searchString) throws SQLException { 
		List<Book> books =  bdao.readAllBooksByName(pageNo, searchString);
		for (Book b: books){
			b.setAuthors(adao.readAllAuthorsByBookId(b.getBookId()));
			b.setGenres(gdao.readAllGenresByBookId(b.getBookId()));
			b.setPublisher(pdao.readPublisherByBookId(b.getBookId()));
		}
		return books;
	}
	
	@RequestMapping(value = "deleteBook", method = RequestMethod.POST, consumes="application/json ")
	public String deleteBook(@RequestBody Book book) throws SQLException {
		bdao.deleteBook(book);
		return "Book deleted successfully!";
	}
			
	
	//================================================================================
    // Authors pages
    //================================================================================

	
//	@RequestMapping(value = "/a_author", method = RequestMethod.GET)
//	public String a_author() {
//		return "a_author";
//	}
	
	@RequestMapping(value = "addAuthor", method = RequestMethod.POST, consumes="application/json")
	public String addAuthor(@RequestBody Author author) throws SQLException {
		adao.addAuthor(author);
		return "Author Added - Success is in the AIR!";
	}
	
	@RequestMapping(value = "editAuthor", method = RequestMethod.POST, 
			consumes="application/json", produces="application/json")
	public List<Author> editBook(@RequestBody Author author) throws SQLException {
		adao.updateAuthor(author);
		return adao.readAllAuthors();
	}
	
	@RequestMapping(value = "/a_viewauthors/{pageNo}/{searchString}", method = RequestMethod.GET, produces="application/json")
	public List<Author> a_viewAuthors(@PathVariable Integer pageNo, 
			@PathVariable String searchString) throws SQLException { 
		List<Author> authors =  adao.readAllAuthorsByName(pageNo, searchString);
		for (Author a: authors){
			a.setBooks(bdao.readAllBooksByAuthorId(a.getAuthorId()));
		}
		return authors;
	}
	
	@RequestMapping(value = "/a_viewauthors/{pageNo}", method = RequestMethod.GET, produces="application/json")
	public List<Author> a_viewAuthors(@PathVariable Integer pageNo) throws SQLException { 
		List<Author> authors =  adao.readAllAuthors(pageNo);
		for (Author a: authors){
			a.setBooks(bdao.readAllBooksByAuthorId(a.getAuthorId()));
		}
		return authors;
	}
	
	@RequestMapping(value = "/a_viewauthors", method = RequestMethod.GET, produces="application/json")
	public List<Author> a_viewAuthors() throws SQLException { 
		List<Author> authors =  adao.readAllAuthors();
		for (Author a: authors){
			a.setBooks(bdao.readAllBooksByAuthorId(a.getAuthorId()));
		}
		return authors;
	}
	
	@RequestMapping(value = "deleteAuthor", method = RequestMethod.POST, consumes="application/json ")
	public String deleteAuthor(@RequestBody Author author) throws SQLException {
		adao.deleteAuthor(author);
		return "Author deleted successfully!";
	}
	
	//================================================================================
    // Borrowers pages
    //================================================================================
	
//	@RequestMapping(value = "/a_borrower", method = RequestMethod.GET)
//	public String a_borrower() {
//		return "a_borrower";
//	}

	@RequestMapping(value = "addBorrower", method = RequestMethod.POST, consumes="application/json")
	public String addBorrower(@RequestBody Borrower borrower) throws SQLException {
		bodao.addBorrower(borrower);
		return "borrower Added - Success is in the AIR!";
	}
	
	@RequestMapping(value = "editBorrower", method = RequestMethod.POST, 
			consumes="application/json", produces="application/json")
	public List<Borrower> editBook(@RequestBody Borrower borrower) throws SQLException {
		bodao.updateBorrower(borrower);
		return bodao.readAllBorrowers();
	}
	
	@RequestMapping(value = "/a_viewborrowers/{pageNo}/{searchString}", method = RequestMethod.GET, produces="application/json")
	public List<Borrower> a_viewborrowers(@PathVariable Integer pageNo, 
			@PathVariable String searchString) throws SQLException { 
		List<Borrower> borrowers =  bodao.readAllBorrowersByName(pageNo, searchString);
		for (Borrower bo: borrowers){
			bo.setBookLoans(bldao.readAllBookLoansByCardNo(bo.getCardNo()));
		}
		return borrowers;
	}
	
	@RequestMapping(value = "/a_viewborrowers/{pageNo}", method = RequestMethod.GET, produces="application/json")
	public List<Borrower> a_viewborrowers(@PathVariable Integer pageNo) throws SQLException { 
		List<Borrower> borrowers =  bodao.readAllBorrowers(pageNo);
		for (Borrower bo: borrowers){
			bo.setBookLoans(bldao.readAllBookLoansByCardNo(bo.getCardNo()));
		}
		return borrowers;
	}
	
	@RequestMapping(value = "/a_viewborrowers", method = RequestMethod.GET, produces="application/json")
	public List<Borrower> a_viewborrowers() throws SQLException { 
		List<Borrower> borrowers =  bodao.readAllBorrowers();
		for (Borrower bo: borrowers){
			bo.setBookLoans(bldao.readAllBookLoansByCardNo(bo.getCardNo()));
		}
		return borrowers;
	}
	
	@RequestMapping(value = "deleteBorrower", method = RequestMethod.POST, consumes="application/json ")
	public String deleteBorrower(@RequestBody Borrower borrower) throws SQLException {
		bodao.deleteBorrower(borrower);
		return "borrower deleted successfully!";
	}
	
	//================================================================================
    // Branches pages
    //================================================================================
	
//	@RequestMapping(value = "/a_branch", method = RequestMethod.GET)
//	public String a_branch() {
//		return "a_branch";
//	}
	
	@RequestMapping(value = "addBranch", method = RequestMethod.POST, consumes="application/json")
	public String addBranch(@RequestBody Branch branch) throws SQLException {
		brdao.addBranch(branch);
		return "branch Added - Success is in the AIR!";
	}
	
	@RequestMapping(value = "editbranch", method = RequestMethod.POST, 
			consumes="application/json", produces="application/json")
	public List<Branch> editBook(@RequestBody Branch branch) throws SQLException {
		brdao.updateBranch(branch);
		return brdao.readAllBranches();
	}
	
	@RequestMapping(value = "/a_viewbranches/{pageNo}/{searchString}", method = RequestMethod.GET, produces="application/json")
	public List<Branch> a_viewbranches(@PathVariable Integer pageNo, 
			@PathVariable String searchString) throws SQLException { 
		List<Branch> branches =  brdao.readAllBranchesByName(pageNo, searchString);
		for (Branch br: branches){
			br.setBooks(bdao.readAllBooksByBranchId(br.getBranchId()));
			br.setBookLoans(bldao.readAllBookLoansByBranchId(br.getBranchId()));
		}
		return branches;
	}
	
	@RequestMapping(value = "/a_viewbranches/{pageNo}", method = RequestMethod.GET, produces="application/json")
	public List<Branch> a_viewbranches(@PathVariable Integer pageNo) throws SQLException { 
		List<Branch> branches =  brdao.readAllBranches(pageNo);
		for (Branch br: branches){
			br.setBooks(bdao.readAllBooksByBranchId(br.getBranchId()));
			br.setBookLoans(bldao.readAllBookLoansByBranchId(br.getBranchId()));
		}
		return branches;
	}
	
	@RequestMapping(value = "/a_viewbranches", method = RequestMethod.GET, produces="application/json")
	public List<Branch> a_viewbranches() throws SQLException { 
		List<Branch> branches =  brdao.readAllBranches();
		for (Branch br: branches){
			br.setBooks(bdao.readAllBooksByBranchId(br.getBranchId()));
			br.setBookLoans(bldao.readAllBookLoansByBranchId(br.getBranchId()));
		}
		return branches;
	}
	
	@RequestMapping(value = "deleteBranch", method = RequestMethod.POST, consumes="application/json ")
	public String deleteBranch(@RequestBody Branch branch) throws SQLException {
		brdao.deleteBranch(branch);
		return "branch deleted successfully!";
	}
	
	//================================================================================
    // Publishers pages
    //================================================================================
	
//	@RequestMapping(value = "/a_publisher", method = RequestMethod.GET)
//	public String a_publisher() {
//		return "a_publisher";
//	}
	
	@RequestMapping(value = "addPublisher", method = RequestMethod.POST, consumes="application/json")
	public String addPublisher(@RequestBody Publisher publisher) throws SQLException {
		pdao.addPublisher(publisher);
		return "publisher Added - Success is in the AIR!";
	}
	
	@RequestMapping(value = "editPublisher", method = RequestMethod.POST, 
			consumes="application/json", produces="application/json")
	public List<Publisher> editPulisher(@RequestBody Publisher publisher) throws SQLException {
		pdao.updatePublisher(publisher);
		return pdao.readAllPublishers();
	}
	
	@RequestMapping(value = "/a_viewpublishers/{pageNo}/{searchString}", method = RequestMethod.GET, produces="application/json")
	public List<Publisher> a_viewpublishers(@PathVariable Integer pageNo, 
			@PathVariable String searchString) throws SQLException { 
		List<Publisher> publishers =  pdao.readAllPublishersByName(pageNo, searchString);
		for (Publisher p:publishers){
			p.setBooks(bdao.readAllBooksByPublisherId(p.getPublisherId()));
		}
		return publishers;
	}
	
	@RequestMapping(value = "/a_viewpublishers/{pageNo}", method = RequestMethod.GET, produces="application/json")
	public List<Publisher> a_viewpublishers(@PathVariable Integer pageNo) throws SQLException { 
		List<Publisher> publishers =  pdao.readAllPublishers(pageNo);
		for (Publisher p:publishers){
			p.setBooks(bdao.readAllBooksByPublisherId(p.getPublisherId()));
		}
		return publishers;
	}
	
	@RequestMapping(value = "/a_viewpublishers", method = RequestMethod.GET, produces="application/json")
	public List<Publisher> a_viewpublishers() throws SQLException { 
		List<Publisher> publishers =  pdao.readAllPublishers();
		for (Publisher p:publishers){
			p.setBooks(bdao.readAllBooksByPublisherId(p.getPublisherId()));
		}
		return publishers;
	}
	
	@RequestMapping(value = "deletePublisher", method = RequestMethod.POST, consumes="application/json ")
	public String deletePublisher(@RequestBody Publisher publisher) throws SQLException {
		pdao.deletePublisher(publisher);
		return "publisher deleted successfully!";
	}
	
	//================================================================================
    // Loans pages
    //================================================================================
	
	
	
	@RequestMapping(value = "editBookLoan", method = RequestMethod.POST, 
			consumes="application/json", produces="application/json")
	public List<BookLoan> editBookLoan(@RequestBody BookLoan bookloan) throws SQLException {
		bldao.updateBookLoan(bookloan);
		return bldao.readAllBookLoans();
	}
	
	@RequestMapping(value = "/a_viewbookloans", method = RequestMethod.GET, produces="application/json")
	public List<BookLoan> a_viewbookloans() throws SQLException {
		return bldao.readAllBookLoans();
	}
	
	@RequestMapping(value = "/a_viewbookloans/{pageNo}", method = RequestMethod.GET, produces="application/json")
	public List<BookLoan> a_viewbookloans(@PathVariable Integer pageNo) throws SQLException {
		return bldao.readAllBookLoans(pageNo);
	}
	
	@RequestMapping(value = "/a_viewbookloans/{pageNo}/{searchString}", method = RequestMethod.GET, produces="application/json")
	public List<BookLoan> a_viewbookloans(@PathVariable Integer pageNo, 
			@PathVariable String searchString) throws SQLException {
		return bldao.readAllBookLoansByDateOut(pageNo, searchString);
	}
}
