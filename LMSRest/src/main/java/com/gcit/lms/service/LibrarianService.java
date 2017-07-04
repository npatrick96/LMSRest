package com.gcit.lms.service;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.gcit.lms.dao.BookCopyDAO;
import com.gcit.lms.dao.BookDAO;
import com.gcit.lms.dao.BookLoanDAO;
import com.gcit.lms.dao.BranchDAO;
import com.gcit.lms.entity.Book;
import com.gcit.lms.entity.BookCopy;
import com.gcit.lms.entity.Branch;

@RestController
public class LibrarianService {
	@Autowired
	BranchDAO brdao;
	
	@Autowired
	BookCopyDAO bcdao;
	
	@Autowired
	BookDAO bdao;
	
	@Autowired
	BookLoanDAO bldao;
	
	@RequestMapping(value = "/librarian", method = RequestMethod.GET, produces="application/json")
	public List<Branch> librarian() throws SQLException { 
		List<Branch> branches =  brdao.readAllBranches();
		for (Branch br: branches){
			br.setBooks(bdao.readAllBooksByBranchId(br.getBranchId()));
			br.setBookLoans(bldao.readAllBookLoansByBranchId(br.getBranchId()));
		}
		return branches;
	}
	
	@Transactional
	@RequestMapping(value = "editBranchLib", method = RequestMethod.POST, 
			consumes="application/json", produces="application/json")
	public List<Branch> editBranchLib(@RequestBody Branch branch) throws SQLException {
		brdao.updateBranch(branch);
		return librarian();
	}
	
	@RequestMapping(value = "/l_viewbookcopies", method = RequestMethod.GET, 
			produces="application/json")
	public List<BookCopy> l_viewbookcopies() throws SQLException {
		return bcdao.readAllBookCopies();
	}
	
	@RequestMapping(value = "/l_viewbookcopiesbybranch", method = RequestMethod.POST, 
			consumes="application/json", produces="application/json")
	public List<BookCopy> l_viewbookcopiesbybranch(@RequestBody Branch branch) throws SQLException {
		return bcdao.readAllBookCopiesByBranch(branch);
	}
	
	@RequestMapping(value = "/l_viewbookcopiesbybook", method = RequestMethod.POST, 
			consumes="application/json", produces="application/json")
	public List<BookCopy> l_viewbookcopiesbybook(@RequestBody Book book) throws SQLException {
		return bcdao.readAllBookCopiesByBook(book);
	}
	
	@Transactional
	@RequestMapping(value = "editBookCopy", method = RequestMethod.POST, 
			consumes="application/json", produces="application/json")
	public List<BookCopy> editBookCopy(@RequestBody BookCopy bookcopy) throws SQLException {
		bcdao.updateBookCopy(bookcopy);
		return l_viewbookcopies();
	}
	
	@Transactional
	@RequestMapping(value = "deleteBookCopy", method = RequestMethod.POST, 
			consumes="application/json", produces="application/json")
	public List<BookCopy> deleteBookCopy(@RequestBody BookCopy bookcopy) throws SQLException {
		bcdao.deleteBookCopy(bookcopy);
		return l_viewbookcopies();
	}
}