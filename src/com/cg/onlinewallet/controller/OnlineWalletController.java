package com.cg.onlinewallet.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.hibernate.usertype.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.ModelAndView;

import com.cg.onlinewallet.dto.TransactionHistory;
import com.cg.onlinewallet.dto.WalletAccount;
import com.cg.onlinewallet.dto.WalletUser;
import com.cg.onlinewallet.excelview.TransactionsExcel;
import com.cg.onlinewallet.exception.MyException;
import com.cg.onlinewallet.service.WalletUserService;


//to do 2 bugs in user functionalities
//get transactions functionality
//validation
//excel sheet upload and download

@Controller
public class OnlineWalletController {
	
	@Autowired
	private WalletUserService service;
	
	private  WalletUser user;
	
	@RequestMapping(value="/home",method=RequestMethod.GET)
	public String homePage() {
		return "home";
	}
	@RequestMapping(value="/login",method=RequestMethod.GET)
	public String loginPage() {
		return "login";
	}
	
	@RequestMapping(value="/userPage",method=RequestMethod.POST)
	public String userPage(@RequestParam("userName") Integer userId,
			@RequestParam("userPassword") String password,
			@RequestParam("userType") String userType,HttpServletRequest req,HttpServletResponse res
			,Map<String,Object> model) {
		
		if(userType.equals("customer")) {
			System.out.println("hii");
			if(service.validateLoginCredentials(userId, password)) {
				Cookie cookie = new Cookie("status","loggedin");
				res.addCookie(cookie);
			//	WalletUser user;
				try {
					user = service.getUser(userId);
					HttpSession session = req.getSession();
					session.setAttribute("user",user);
					model.put("user",user);
					return "UserFunctionalitiesPage";
				} catch (MyException e) {
					// TODO Auto-generated catch block
					System.out.println(e.getMessage());
				}
				return "login";
			}
		}
		else{
			if(userId==1&&password.equals("venkatesh")) {
				Cookie cookie = new Cookie("status","loggedin");
				res.addCookie(cookie);
				model.put("name", "Venkatesh");
				return "AdminFunctionalitiesPage";
			}
		}
		return "login";
	}
	@RequestMapping(value="/registration",method=RequestMethod.GET)
	public String userRegistrationPage(@ModelAttribute("registrationForm") WalletUser user) {
		return "registration";
	}
	@RequestMapping(value="/getRegistrationDetails",method = RequestMethod.POST)
	public String getRegistrationDetails(@Valid@ModelAttribute("registrationForm") WalletUser user
			,@RequestParam("confirmpassword") String confirmpassword,BindingResult br) {
		
		System.out.println("hii");
		
		if(br.hasErrors()) {
			System.out.println("hii");
			return "registration";
		}
		
		System.out.println(user);
		if(confirmpassword.equals(user.getUserPassword())) {
			try {
				user.setAccount(new WalletAccount());;
				service.addWalletUser(user);
			} catch (MyException e) {
				// TODO Auto-generated catch block
				System.out.println(e.getMessage());
			}
		}
		else {
			return "registration";
		}
		return "login";
	}
	@RequestMapping(value="/viewAccountsToBeApproved",method = RequestMethod.GET)
	public String viewAccountsToBeApproved(Map<String,Object> model) {
		List<WalletAccount> accounts = service.accountsToBeApproved();
		model.put("accounts", accounts);
		return "accountsToBeApprovedPage";
	}
	@RequestMapping(value="/approveAccount",method=RequestMethod.GET)
	public String approveAccountPage() {
		return "approveAccount";
	}
	@RequestMapping(value="/getApproveAccountNo",method=RequestMethod.POST)
	public String getApproveAccountNo(@RequestParam("accountNo") Integer accountNo) {
		try {
			service.approveAccount(accountNo);
		} catch (MyException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
		return "AdminFunctionalitiesPage";
	}
	@RequestMapping(value="/signOut",method=RequestMethod.GET)
	public String signOut(HttpServletRequest req,HttpServletResponse res) {
		Cookie cookies[] = req.getCookies();
		for(Cookie c:cookies) {
			if(c.getName().equals("status")){
				System.out.println("out");
				System.out.println(c.getValue());
			}
		}
	    Cookie c = new Cookie("status","");
	    c.setMaxAge(0);
	    res.addCookie(c);
		return "home";
	}
	@RequestMapping(value="/addAmount",method=RequestMethod.GET)
	public String addAmount(HttpServletResponse res,HttpServletRequest request,Map<String,Object> model) {
		//WalletUser user;
			HttpSession seesion = request.getSession();
			user = (WalletUser)seesion.getAttribute("user");
			System.out.println(user.getPhoneNo());
		    model.put("user",user);
		return "addAmount";
	}
	@RequestMapping(value="/redirectAfterTransaction",method=RequestMethod.GET)
	public String redirectAfterTransaction(HttpServletRequest req) {
		HttpSession session = req.getSession();
		user=(WalletUser)session.getAttribute("user");
		try {
			req.setAttribute("user",service.getUser(user.getUserId()));
		} catch (MyException e) {
			// TODO Auto-generated catch block
		   
		}
		return "UserFunctionalitiesPage";
	}
	@RequestMapping(value="/getAmount",method=RequestMethod.POST)
	public String getAmount(@RequestParam("amount") Double amount,HttpServletRequest request
			,Map<String,Object> model) {
		//WalletUser user;
		try {
			HttpSession seesion = request.getSession();
			user =(WalletUser)seesion.getAttribute("user");
			System.out.println(user.getPhoneNo());
			if(!user.getAccount().getAccountStatus().toString().equals("WatingForApproval")) {
				service.addAmount(user.getUserId(), amount);
				amount=0.0;
				model.put("user",service.getUser(user.getUserId()));
			}
		} catch (MyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("hii");
		return "redirect:/redirectAfterTransaction";
	}
	@RequestMapping(value="/transferAmount",method=RequestMethod.POST)
	public String transferAmount(HttpServletResponse res,HttpServletRequest request,Map<String,Object> model,
			@RequestParam("accountType") String accountType,@RequestParam("phoneNo") String phoneNo
			,@RequestParam("amount") Double amount) {
		//WalletUser user;
		HttpSession seesion = request.getSession();
		user = (WalletUser)seesion.getAttribute("user");
		System.out.println(user.getPhoneNo());
	    System.out.println("good u entered");
	    if(accountType.equals("same")) {
	    	try {
	    		if(!user.getAccount().getAccountStatus().toString().equals("WatingForApproval")) {
	    			service.transferAmount(user.getUserId(), phoneNo, amount);
	    		}
				
			} catch (MyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    else {
	    	try {
	    		if(!user.getAccount().getAccountStatus().toString().equals("WatingForApproval")) {
	    			service.transferAmount(user.getUserId(), Integer.parseInt(phoneNo), amount);
	    		}
			} catch (NumberFormatException | MyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    return "redirect:/redirectAfterTransaction";
	}
	
	 @RequestMapping(value="/getTransactions",method=RequestMethod.GET)
     public ModelAndView getExcel(){
            List<TransactionHistory> transactionList = new ArrayList<TransactionHistory>(); 
            
            return new ModelAndView(new TransactionsExcel(), "transactionList", transactionList);
     }
	

}
