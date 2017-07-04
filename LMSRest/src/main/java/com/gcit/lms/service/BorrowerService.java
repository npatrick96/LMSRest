package com.gcit.lms.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
	
	@RequestMapping(value = "/b_viewbookloansbyuser", method = RequestMethod.GET, 
			consumes="application/json", produces="application/json")
	public List<BookLoan> b_viewbookloansbyuser(@RequestBody Borrower borrower) throws SQLException {
		return bldao.readAllBookLoansByBorrower(borrower);
	}
	
	@RequestMapping(value = "/b_viewbookloansbyuser/{cardNo}", 
			method = RequestMethod.GET, produces="application/json")
	public List<BookLoan> b_viewbookloansbyuser(@PathVariable Integer cardNo) throws SQLException {
		return bldao.readAllBookLoansByCardNo(cardNo);
	}
	
	@RequestMapping(value = "/b_viewduebookloansbyuser", method = RequestMethod.GET, 
			consumes="application/json", produces="application/json")
	public List<BookLoan> b_viewduebookloansbyuser(@RequestBody Borrower borrower) throws SQLException {
		return bldao.readAllDueBookLoansByBorrower(borrower);
	}
	
	@RequestMapping(value = "/b_viewduebookloansbyuser/{cardNo}", 
			method = RequestMethod.GET, produces="application/json")
	public List<BookLoan> b_viewduebookloansbyuser(@PathVariable Integer cardNo) throws SQLException {
		return bldao.readAllDueBookLoansByCardNo(cardNo);
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
	
	@RequestMapping(value = "borrowerLogin/{cardNo}", method = RequestMethod.GET)
	public String borrowerLogin(@PathVariable Integer cardNo) throws SQLException {
		if ((cardNo != null) && (cardNo > 0)){
			Integer num =  bodao.getBorrowersCountByPk(cardNo);
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
	
	@Transactional
	@RequestMapping(value = "/checkOutBook/{bookId}/{branchId}/{cardNo}", 
			method = RequestMethod.POST, produces="application/json")
	public String checkOutBook(@PathVariable Integer bookId, 
			@PathVariable Integer branchId, @PathVariable Integer cardNo) throws SQLException { 
		Branch branch = new Branch();
		Book book = new Book();
		Borrower borrower = new Borrower();

		branch.setBranchId(branchId);
		book.setBookId(bookId);
		borrower.setCardNo(cardNo);

		BookLoan bookloan = new BookLoan();

		bookloan.setBook(book);
		bookloan.setBorrower(borrower);
		bookloan.setBranch(branch);

		LocalDateTime todayDateTime = LocalDateTime.now();
		bookloan.setDateOut(todayDateTime + "");
		bookloan.setDueDate(todayDateTime.plusWeeks(1) + "");

		bcdao.decrementNoOfCopiesToZero(bookId, branchId);
		bcdao.decrementNoOfCopies(bookId, branchId);
		bldao.addBookLoan(bookloan);
		return "Book borrowed successfully"; 
		//return b_viewbookloansbyuser(cardNo);
	}
	
	@Transactional
	@RequestMapping(value = "returnBook/{bookId}/{branchId}/{cardNo}/{dateOut}", 
			method = RequestMethod.POST, produces="application/json")
	public String returnBook(@PathVariable Integer bookId, @PathVariable Integer branchId, 
			@PathVariable Integer cardNo, @PathVariable String dateOut) throws SQLException { 
		BookLoan bookloan  = bldao.readBookLoanBy4Pks(bookId, branchId, cardNo, dateOut);
		
		bldao.returnBook(bookloan);
		BookCopy bookCopy = new BookCopy();
		bookCopy.setBook(bookloan.getBook());
		bookCopy.setBranch(bookloan.getBranch());
		if (bcdao.getBookCopiesCount(bookCopy) > 0) {
			bcdao.incrementNoOfCopies(bookloan.getBook().getBookId(), bookloan.getBranch().getBranchId());
		} else {
			bookCopy.setNoOfCopies(1);
			bcdao.addBookCopy(bookCopy);
		}
		return "Book returned successfully"; 
		//return b_viewbookloansbyuser(cardNo);
	}
	
	
	@Transactional
	@RequestMapping(value = "returnBook", method = RequestMethod.POST, 
	consumes="application/json", produces="application/json")
	public String returnBook(@RequestBody BookLoan bookloan) throws SQLException { 
		bldao.returnBook(bookloan);
		BookCopy bookCopy = new BookCopy();
		bookCopy.setBook(bookloan.getBook());
		bookCopy.setBranch(bookloan.getBranch());
		if (bcdao.getBookCopiesCount(bookCopy) > 0) {
			bcdao.incrementNoOfCopies(bookloan.getBook().getBookId(), bookloan.getBranch().getBranchId());
		} else {
			bookCopy.setNoOfCopies(1);
			bcdao.addBookCopy(bookCopy);
		}
		return "Book returned successfully"; 
		//return b_viewbookloansbyuser(bookloan.getBorrower().getCardNo());
	}
}
