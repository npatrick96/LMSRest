package com.gcit.lms.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gcit.lms.dao.BookCopyDAO;
import com.gcit.lms.dao.BookDAO;
import com.gcit.lms.dao.BookLoanDAO;
import com.gcit.lms.dao.BorrowerDAO;
import com.gcit.lms.dao.BranchDAO;
import com.gcit.lms.entity.Book;
import com.gcit.lms.entity.BookCopy;
import com.gcit.lms.entity.BookLoan;
import com.gcit.lms.entity.Borrower;
import com.gcit.lms.entity.Branch;

@RestController
public class BorrowerService {
	@Autowired
	BorrowerDAO bodao;
	
	@Autowired
	BookDAO bdao;
	
	@Autowired
	BranchDAO brdao;
	
	@Autowired
	BookLoanDAO bldao;
	
	@Autowired
	BookCopyDAO bcdao;
	
	//================================================================================
    // BORROWER SERVICES
    //================================================================================
	
	@RequestMapping(value = "/b_viewbookloansbyuser", method = RequestMethod.POST, 
			consumes="application/json", produces="application/json")
	public List<BookLoan> b_viewbookloansbyuser(@RequestBody Borrower borrower) throws SQLException {
		return bldao.readAllDueBookLoansByBorrower(borrower);
	}
	
	@RequestMapping(value = "borrowerLogin", method = RequestMethod.POST, consumes="application/json")
	public String borrowerLogin(@RequestBody Borrower borrower) throws SQLException {
		if ((borrower.getCardNo() != null) && (borrower.getCardNo() > 0)){
			Integer num =  bodao.getBorrowersCountByPk(borrower.getCardNo());
			if(num > 0){
				return "Logged in successfully!";
			}else{
				return "Please enter a valid card number!";
			}
		}
		return "Borrower card number should be a positive number!";
	}
	
	@RequestMapping(value = "/b_viewbooksavailableatbranch", method = RequestMethod.POST, 
			consumes="application/json",produces="application/json")
	public List<BookCopy> b_viewbooksavailableatbranch(@RequestBody Branch branch) throws SQLException { 
		return bcdao.readAllBookCopiesByBranch(branch);
	}
	
//	@RequestMapping(value = "/checkOutBook", method = RequestMethod.GET)
//	public String checkOutBook(Model model,@RequestParam("cardNo") Integer cardNo,
//			@RequestParam("branchId") Integer branchId, @RequestParam("bookId") Integer bookId) throws SQLException {
//		Borrower borrower = borrowerService.getBorrowerByPK(cardNo);
//		borrowerService.checkOutBook(bookId, branchId, cardNo);
//		String message = borrower.getName()+". Book borrowed successfully";
//		model.addAttribute("message", message);
//		model.addAttribute("cardNo", cardNo);
//		return "b_pickaction";
//	}
	
	// TODO return book copy  // COPY STUFF from borrower service
	
   //  TODO check out book copy
	
//	@RequestMapping(value = "/returnBook", method = RequestMethod.GET)
//	public String returnBook(Model model,@RequestParam("cardNo") Integer cardNo,
//			@RequestParam("branchId") Integer branchId, 
//			@RequestParam("bookId") Integer bookId,
//			@RequestParam("dateOut") String dateOut) throws SQLException {
//		
//		BookLoan bookLoan = new BookLoan();
//		dateOut = dateOut.replaceAll("T", " ");
//		
//		bookLoan.setBook(adminService.getBookByPK(bookId));
//		bookLoan.setBorrower(adminService.getBorrowerByPK(cardNo));
//		bookLoan.setBranch(adminService.getBranchByPK(branchId));
//		bookLoan.setDateOut(dateOut);
//		borrowerService.returnBook(bookLoan);
//		
//		Borrower borrower = borrowerService.getBorrowerByPK(cardNo);
//		String message = "Thank you "+borrower.getName()+"! Book returned successfully!";
//		model.addAttribute("message", message);
//		return null; //b_viewbookloans(model,cardNo);
//	}
	
	
	//================================================================================
    // BELOW HERE IS OLD BORROWER SERVICE CODE TERRITORY
    //================================================================================

	
//	public Borrower getBorrowerByPK(Integer authorId) throws SQLException {
//		return bodao.readBorrowerByPK(authorId);
//	}
//	
//	public Boolean isValidCardNo(Integer cardNo) throws SQLException {
//		Integer num =  bodao.getBorrowersCountByPk(cardNo);
//		if(num > 0){
//			return true;
//		}else{
//			return false;
//		}
//	}
//	
//	@Transactional
//	public void checkOutBook(Integer bookId, Integer branchId, Integer cardNo) throws SQLException {
//		Branch branch = new Branch();
//		Book book = new Book();
//		Borrower borrower = new Borrower();
//
//		branch.setBranchId(branchId);
//		book.setBookId(bookId);
//		borrower.setCardNo(cardNo);
//
//		BookLoan bookloan = new BookLoan();
//
//		bookloan.setBook(book);
//		bookloan.setBorrower(borrower);
//		bookloan.setBranch(branch);
//
//		LocalDateTime todayDateTime = LocalDateTime.now();
//		bookloan.setDateOut(todayDateTime + "");
//		bookloan.setDueDate(todayDateTime.plusWeeks(1) + "");
//
//		bcdao.decrementNoOfCopiesToZero(bookId, branchId);
//		bcdao.decrementNoOfCopies(bookId, branchId);
//		bldao.addBookLoan(bookloan);
//		// TODO: Test it and make sure it works
//	}
//	
//	@Transactional
//	public void returnBook(BookLoan bookLoan) throws SQLException {
//		bldao.returnBook(bookLoan);
//		BookCopy bookCopy = new BookCopy();
//		bookCopy.setBook(bookLoan.getBook());
//		bookCopy.setBranch(bookLoan.getBranch());
//		if (bcdao.getBookCopiesCount(bookCopy) > 0) {
//			bcdao.incrementNoOfCopies(bookLoan.getBook().getBookId(), bookLoan.getBranch().getBranchId());
//		} else {
//			bookCopy.setNoOfCopies(1);
//			bcdao.addBookCopy(bookCopy);
//		}
//	}
//	
//	public List<Branch> getAllBranches() throws SQLException {
//		return brdao.readAllBranches();
//	}
//	
//	public List<Book> getAllBooksOwned(Branch branch) throws SQLException {
//		return bdao.readAllBooksByBranch(branch);
//	}
}
