package org.mars_sim.msp.demo;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.Timer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;

@SuppressWarnings("serial")
public class PopGUserInterface extends JPanel implements ActionListener{

	private JPanel displayPanel;
	private JFrame frmPopG;
	private JMenuBar menuFile;
	private JMenu mnFile;
	private JMenuItem mntmSave;
	private JMenuItem mntmPrint;
	private JMenuItem mntmAbout;
	private JMenuItem mntmQuit;
	private JMenu mnRun;
	private JMenuItem mntmContinuew;
	private JMenuItem mntmContinue;
	private JMenuItem mntmNewRun;
	private JMenuItem mntmRestart;
	private JMenuItem mntmWholePlot;
	public int numberOfGen;
	public Graphics2D g2d;
	private PopGData inputvals;
	private JPanel contentPane;
	private JTextField txtPopSize;
	private JTextField txtFitGenAA;
	private JLabel lblPopSize;
	private JLabel lblFitGenAA;
	private JLabel lblFitGenAa;
	private JTextField txtFitGenAa;
	private JLabel lblFitGenaa;
	private JTextField txtFitGenaa;
	private JLabel lblMutAa;
	private JTextField txtMutAa;
	private JLabel lblMutaA;
	private JTextField txtMutaA;
	private JLabel lblMigRate;
	private JTextField txtMigRate;
	private JLabel lblInitFreq;
	private JTextField txtInitFreq;
	private JLabel lblGenRun;
	public JTextField txtGenRun;
	private JLabel lblNumPop;
	private JTextField txtNumPop;
	private JLabel lblRandSeed;
	private JTextField txtRandSeed;
	private JButton btnOK;
	private JButton btnQuit;
	private JButton btnDefaults;
	public double numberSeed1;
	private Random numberSeed;
	private JFrame frmPopGSettingsMenu;
	private JFrame frmContinue;
	private JLabel lblContinueGenRun;
	private JTextField txtContinueGenRun;
	private JButton btnContinueGenRunOK;
	private JButton btnContinueGenCancel;
	private JFrame frmAbout;
	
	private ArrayList<ArrayList<Double>> genArray;
	private int xOffset;
	private int yOffset;
	private int xLen;
	private int yLen;
	private int numFixedPops;
	private int numLostPops;
	private int beginGen;
	private int endGen;
	private Timer timer;
	private static final int TIMELEN = 1;
	private String filedir;
	
	public class PopGData {
		Integer popSize;
		Double fitGenAA;
		Double fitGenAa;
		Double fitGenaa;
		Double mutAa;
		Double mutaA;
		Double migRate;
		Double initFreq;
		Integer genRun;
		Integer numPop;
		Boolean genSeed;
		Long randSeed;
	}
	
	/**
	 * Launch the application.
	 */
	

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PopGUserInterface frame = new PopGUserInterface();
					frame.frmPopG.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Create the frame.
	 */

	public PopGUserInterface() {
		// initialize data
		inputvals = new PopGData();
		initInputVals();
   	 	genArray = new ArrayList<ArrayList<Double>>();
		filedir = System.getProperty("user.dir");

        frmPopG = new JFrame();
        frmPopG.setTitle("PopG");
        frmPopG.setSize(650, 430);
        frmPopG.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmPopG.getContentPane().setLayout(null);
        frmPopG.setVisible(true);
   	 	setBounds(100, 100, 485, 382);
   	 	contentPane = new JPanel();
   	 	contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
   	 	contentPane.setLayout(null);
   	 	
   	 	displayPanel = new JPanel()
        {
            public void paintComponent(Graphics graph)
            {
                draw(graph);
            }
        };
	    displayPanel.setBackground(Color.WHITE);
	    displayPanel.setBounds(0, 0, 5000, 5000);
	    displayPanel.setLayout(null);
	    frmPopG.getContentPane().add(displayPanel);
  	 	         
        menuFile = new JMenuBar();
        frmPopG.setJMenuBar(menuFile);
        
        mnFile = new JMenu("File");
        menuFile.add(mnFile);
        
        mnRun = new JMenu("Run");
        menuFile.add(mnRun);
        mntmContinuew = new JMenuItem("Continue w/ 100");
        mntmContinuew.setEnabled(false);
        mntmContinuew.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
				runPopGThreads(false, inputvals.genRun);
        	}
        });
        mnRun.add(mntmContinuew);
                        
        mntmContinue = new JMenuItem("Continue");
        mntmContinue.setEnabled(false);
        mntmContinue.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		frmContinue = new JFrame();
        		frmContinue.setVisible(true);
        		frmContinue.setBounds(20, 50, 300, 130);
        		frmContinue.setTitle("enter a number");
        		frmContinue.setLayout(null);
        		frmContinue.setResizable(false);
        		
        		lblContinueGenRun = new JLabel("How many generations to run?");
        		lblContinueGenRun.setBounds(50, 10, 200, 14);
        		frmContinue.add(lblContinueGenRun);
        		
        		txtContinueGenRun = new JTextField();
        		txtContinueGenRun.setText(inputvals.genRun.toString());
        		txtContinueGenRun.setBounds(35, 45, 200, 20);
        		frmContinue.add(txtContinueGenRun);
        		
        		btnContinueGenRunOK = new JButton();
        		btnContinueGenRunOK.setText("OK");
        		btnContinueGenRunOK.setBounds(205, 75, 84, 25);
        		btnContinueGenRunOK.addActionListener(new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
         				int continueGenRun = Integer.parseInt(txtContinueGenRun.getText());
           				frmContinue.dispose();
      					inputvals.genRun = continueGenRun;
        				mntmContinuew.setText("Continue w/ " + inputvals.genRun);
           				runPopGThreads(false, continueGenRun);        					   				
       			    }
        		});
        		frmContinue.add(btnContinueGenRunOK);
        		
        		btnContinueGenCancel = new JButton();
        		btnContinueGenCancel.setText("Cancel");
        		btnContinueGenCancel.setBounds(125, 75, 84, 25);
        		btnContinueGenCancel.addActionListener(new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
           				frmContinue.dispose();
     					frmPopG.repaint();
      			    }
        		});
        		frmContinue.add(btnContinueGenCancel);
        	}
        });
        mnRun.add(mntmContinue);
    	
        mntmNewRun = new JMenuItem("New Run");
        mntmNewRun.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {     		
        		frmPopGSettingsMenu = new JFrame();
        		frmPopGSettingsMenu.setVisible(true);
        		frmPopGSettingsMenu.setBounds(100, 50, 480, 350);
        		frmPopGSettingsMenu.setTitle("PopG Settings");
        		frmPopGSettingsMenu.setLayout(null);
         		
        		lblPopSize = new JLabel("Population size:");
        		lblPopSize.setBounds(10, 10, 275, 14);
        		lblPopSize.setHorizontalAlignment(SwingConstants.TRAILING);
        		frmPopGSettingsMenu.add(lblPopSize);
        		
        		txtPopSize = new JTextField();
        		txtPopSize.setText(inputvals.popSize.toString());
        		txtPopSize.setBounds(295, 8, 151, 20);
        		frmPopGSettingsMenu.add(txtPopSize);
        		txtPopSize.setColumns(10);
        		
        		lblFitGenAA = new JLabel("Fitness of genotype AA:");
        		lblFitGenAA.setBounds(10, 35, 275, 14);
        		lblFitGenAA.setHorizontalAlignment(SwingConstants.TRAILING);
        		frmPopGSettingsMenu.add(lblFitGenAA);
        		
        		txtFitGenAA = new JTextField();
        		txtFitGenAA.setText(inputvals.fitGenAA.toString());
        		txtFitGenAA.setBounds(295, 33, 151, 20);
        		frmPopGSettingsMenu.add(txtFitGenAA);
        		txtFitGenAA.setColumns(10);
        		
        		lblFitGenAa = new JLabel("Fitness of genotype Aa:");
        		lblFitGenAa.setBounds(10, 60, 275, 14);
        		lblFitGenAa.setHorizontalAlignment(SwingConstants.TRAILING);
        		frmPopGSettingsMenu.add(lblFitGenAa);

        		txtFitGenAa = new JTextField();
        		txtFitGenAa.setText(inputvals.fitGenAa.toString());
        		txtFitGenAa.setBounds(295, 58, 151, 20);
        		frmPopGSettingsMenu.add(txtFitGenAa);
        		txtFitGenAa.setColumns(10);

        		lblFitGenaa = new JLabel("Fitness of genotype aa:");
        		lblFitGenaa.setBounds(10, 85, 275, 14);
        		lblFitGenaa.setHorizontalAlignment(SwingConstants.TRAILING);
        		frmPopGSettingsMenu.add(lblFitGenaa);

        		txtFitGenaa = new JTextField();
        		txtFitGenaa.setText(inputvals.fitGenaa.toString());
        		txtFitGenaa.setBounds(295, 83, 151, 20);
        		frmPopGSettingsMenu.add(txtFitGenaa);
        		txtFitGenaa.setColumns(10);

        		lblMutAa = new JLabel("Mutation from A to a:");
        		lblMutAa.setBounds(10, 110, 275, 14);
        		lblMutAa.setHorizontalAlignment(SwingConstants.TRAILING);
        		frmPopGSettingsMenu.add(lblMutAa);

        		txtMutAa = new JTextField();
        		txtMutAa.setText(inputvals.mutAa.toString());
        		txtMutAa.setBounds(295, 108, 151, 20);
        		frmPopGSettingsMenu.add(txtMutAa);
        		txtMutAa.setColumns(10);

        		lblMutaA = new JLabel("Mutation from a to A:");
        		lblMutaA.setBounds(10, 135, 275, 14);
        		lblMutaA.setHorizontalAlignment(SwingConstants.TRAILING);
        		frmPopGSettingsMenu.add(lblMutaA);

        		txtMutaA = new JTextField();
        		txtMutaA.setText(inputvals.mutaA.toString());
        		txtMutaA.setBounds(295, 133, 151, 20);
        		frmPopGSettingsMenu.add(txtMutaA);
        		txtMutaA.setColumns(10);

        		lblMigRate = new JLabel("Migration rate between populations:");
        		lblMigRate.setBounds(10, 160, 275, 14);
        		lblMigRate.setHorizontalAlignment(SwingConstants.TRAILING);
        		frmPopGSettingsMenu.add(lblMigRate);

        		txtMigRate = new JTextField();
        		txtMigRate.setText(inputvals.migRate.toString());
        		txtMigRate.setBounds(295, 158, 151, 20);
        		frmPopGSettingsMenu.add(txtMigRate);
        		txtMigRate.setColumns(10);

        		lblInitFreq = new JLabel("Initial freqency of allele A:");
        		lblInitFreq.setBounds(10, 185, 275, 14);
        		lblInitFreq.setHorizontalAlignment(SwingConstants.TRAILING);
        		frmPopGSettingsMenu.add(lblInitFreq);

        		txtInitFreq = new JTextField();
        		txtInitFreq.setText(inputvals.initFreq.toString());
        		txtInitFreq.setBounds(295, 183, 151, 20);
        		frmPopGSettingsMenu.add(txtInitFreq);
        		txtInitFreq.setColumns(10);

        		lblGenRun = new JLabel("Generations to run:");
        		lblGenRun.setBounds(10, 210, 275, 14);
        		lblGenRun.setHorizontalAlignment(SwingConstants.TRAILING);
        		frmPopGSettingsMenu.add(lblGenRun);

        		txtGenRun = new JTextField();
        		txtGenRun.setText(inputvals.genRun.toString());
        		txtGenRun.setBounds(295, 208, 151, 20);
        		frmPopGSettingsMenu.add(txtGenRun);
        		txtGenRun.setColumns(10);

        		lblNumPop = new JLabel("Populations evolving simultaneously:");
        		lblNumPop.setHorizontalAlignment(SwingConstants.TRAILING);
        		lblNumPop.setBounds(10, 235, 275, 14);
        		frmPopGSettingsMenu.add(lblNumPop);

        		txtNumPop = new JTextField();
        		txtNumPop.setText(inputvals.numPop.toString());
        		txtNumPop.setBounds(295, 233, 151, 20);
        		frmPopGSettingsMenu.add(txtNumPop);
        		txtNumPop.setColumns(6);

        		lblRandSeed = new JLabel("Random number seed:");
        		lblRandSeed.setHorizontalAlignment(SwingConstants.TRAILING);
        		lblRandSeed.setBounds(10, 260, 275, 14);
        		frmPopGSettingsMenu.add(lblRandSeed);

        		txtRandSeed = new JTextField();
        		if (inputvals.genSeed)
        		{
        			txtRandSeed.setText("(Autogenerate)");
        		}
        		else
        		{
        			txtRandSeed.setText(inputvals.randSeed.toString());
        		}
        		txtRandSeed.setBounds(295, 258, 151, 20);
        		frmPopGSettingsMenu.add(txtRandSeed);
        		txtRandSeed.setColumns(6);
        		
        		btnDefaults = new JButton("Defaults");
        		btnDefaults.setBounds(180, 285, 84, 25);
        		btnDefaults.addActionListener(new ActionListener() {
                	public void actionPerformed(ActionEvent e) {
                		initInputVals();                		
                		txtPopSize.setText(inputvals.popSize.toString());
                		txtFitGenAA.setText(inputvals.fitGenAA.toString());
                		txtFitGenAa.setText(inputvals.fitGenAa.toString());
                		txtFitGenaa.setText(inputvals.fitGenaa.toString());
                		txtMutAa.setText(inputvals.mutAa.toString());
                		txtMutaA.setText(inputvals.mutaA.toString());
                		txtMigRate.setText(inputvals.migRate.toString());
                		txtInitFreq.setText(inputvals.initFreq.toString());
                		txtGenRun.setText(inputvals.genRun.toString());
                   		txtNumPop.setText(inputvals.numPop.toString());
                   		if (inputvals.genSeed)
                		{
                			txtRandSeed.setText("(Autogenerate)");
                		}
                		else
                		{
                			txtRandSeed.setText(inputvals.randSeed.toString());
                		}
                 	}
                });
         		frmPopGSettingsMenu.add(btnDefaults);

        		
        		btnQuit = new JButton("Cancel");
        		btnQuit.setBounds(270, 285, 84, 25);
        		btnQuit.addActionListener(new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
    					frmPopGSettingsMenu.dispose();
    					displayPanel.repaint();
        			}
        		});
           		frmPopGSettingsMenu.add(btnQuit);

        		btnOK = new JButton("OK");
        		btnOK.setBounds(362, 285, 84, 25);
        		btnOK.addActionListener(new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        	       		genArray.clear();
        				frmPopG.repaint();
        				String msg;
        				boolean runtests = true;
        				try
        				{
        					inputvals.popSize = Integer.parseInt(txtPopSize.getText());
        				}
        				catch (NumberFormatException nfe)
        				{
        					msg = "Population Size must be an integer";
        					JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
        					runtests = false;
        				}
        				
           				try
        				{
           					inputvals.fitGenAA = Double.parseDouble(txtFitGenAA.getText());
        				}
        				catch (NumberFormatException nfe)
        				{
        					msg = "Fitness of Genotype AA must be a number";
        					JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
        					runtests = false;
        				}
           				
           				try
        				{
           					inputvals.fitGenAa = Double.parseDouble(txtFitGenAa.getText());
        				}
        				catch (NumberFormatException nfe)
        				{
        					msg = "Fitness of Genotype Aa must be a number";
        					JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
        					runtests = false;
        				}
           				
           				try
        				{
           					inputvals.fitGenaa = Double.parseDouble(txtFitGenaa.getText());
        				}
        				catch (NumberFormatException nfe)
        				{
        					msg = "Fitness of Genotype aa must be a number";
        					JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
        					runtests = false;
        				}
           				
           				try
        				{
           					inputvals.mutAa = Double.parseDouble(txtMutAa.getText());
        				}
        				catch (NumberFormatException nfe)
        				{
        					msg = "Mutations from A to a must be a number";
        					JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
        					runtests = false;
        				}
           				
           				try
        				{
           					inputvals.mutaA = Double.parseDouble(txtMutaA.getText());
        				}
        				catch (NumberFormatException nfe)
        				{
        					msg = "Mutations from a to A must be a number";
        					JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
        					runtests = false;
        				}
           				
           				try
        				{
           					inputvals.migRate = Double.parseDouble(txtMigRate.getText());
        				}
        				catch (NumberFormatException nfe)
        				{
        					msg = "Migration Rate must be a number";
        					JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
        					runtests = false;
        				}
           				
           				try
        				{
           					inputvals.initFreq = Double.parseDouble(txtInitFreq.getText());
        				}
        				catch (NumberFormatException nfe)
        				{
        					msg = "Initial Frequency of A must be a number";
        					JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
        					runtests = false;
        				}
           				
           				try
        				{
           					inputvals.genRun = Integer.parseInt(txtGenRun.getText());
        				}
        				catch (NumberFormatException nfe)
        				{
        					msg = "Generations to run must be an integer";
        					JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
        					runtests = false;
        				}
           				
           				try
        				{
           					inputvals.numPop = Integer.parseInt(txtNumPop.getText());
        				}
        				catch (NumberFormatException nfe)
        				{
        					msg = "Populations evolving must be an integer";
        					JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
        					runtests = false;
        				}
           				
        				try {
        					long longVal =Long.parseLong(txtRandSeed.getText());
 	       					inputvals.genSeed = false;
	       					inputvals.randSeed = longVal;
	       					numberSeed = new Random(inputvals.randSeed); 
    					}
        				catch (NumberFormatException nfe)
        				{
         					inputvals.genSeed = true;
          					numberSeed = new Random(); 
        				}
        				
        				if (runtests)
        				{
	         				mntmContinuew.setEnabled(true);
	        				mntmContinue.setEnabled(true);
	        				mntmRestart.setEnabled(true);
	        				mntmContinuew.setText("Continue w/ " + inputvals.genRun);
	        				mntmWholePlot.setEnabled(true);
	        		        mntmSave.setEnabled(true);
	        		        mntmPrint.setEnabled(true);
	        				if(checkInputVals()) {
	        					frmPopGSettingsMenu.dispose();
	         	       			runPopGThreads(true, inputvals.genRun);        					
	         				}
        				}
        			}
        		});
 
        		frmPopGSettingsMenu.add(btnOK);
        	}
          });
        mnRun.add(mntmNewRun);
                                        
        mntmRestart = new JMenuItem("Restart");
        mntmRestart.setEnabled(false);
        mntmRestart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				frmPopG.repaint();
				if(inputvals.genSeed)
				{
 					numberSeed = new Random(); 
				}
				else
				{
   					numberSeed = new Random(inputvals.randSeed); 
				}
				runPopGThreads(true, inputvals.genRun);
            }
        });
        mnRun.add(mntmRestart);
        
        mntmWholePlot = new JMenuItem("Display whole plot");
        mntmWholePlot.setEnabled(false);
        mntmWholePlot.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
				beginGen = 0;
				frmPopG.repaint();
       	}
        });
        mnRun.add(mntmWholePlot);
        
        frmPopG.setVisible(true);
       
        mntmSave = new JMenuItem("Save");
        mntmSave.setEnabled(false);
        mntmSave.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		savePopG();
        	}
       });
        
        mnFile.add(mntmSave);
        frmPopG.setVisible(true);

        mntmPrint = new JMenuItem("Print");
        mntmPrint.setEnabled(false);
        mntmPrint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		        printPopG();
		    }
		});
        
        mnFile.add(mntmPrint);
        frmPopG.setVisible(true);

        mntmAbout = new JMenuItem("About");
        mntmAbout.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		frmAbout = new JFrame();
        		frmAbout.setVisible(true);
        		frmAbout.setBounds(100, 50, 630, 300);
        		frmAbout.setTitle("About PopG");
        		frmAbout.setLayout(null);
 
        		JLabel lblLine1 = new JLabel("Copyright 1993-2013. University of Washington and Joseph Felsenstein. All rights reserved.");
        		lblLine1.setBounds(10, 10, 600, 14);
        		frmAbout.add(lblLine1);
        		
        		JLabel lblLine2 = new JLabel("Permission is granted to reproduce, perform, and modify this program.");
        		lblLine2.setBounds(10, 30, 600, 14);
        		frmAbout.add(lblLine2);
           		
        		JLabel lblLine3 = new JLabel("Permission is granted to distribute or provide access to this program provided that:");
        		lblLine3.setBounds(10, 50, 600, 14);
        		frmAbout.add(lblLine3);
           		
        		JLabel lblLine4 = new JLabel("1) this copyright notice is not removed");
        		lblLine4.setBounds(10,70, 600, 14);
        		frmAbout.add(lblLine4);
           		
        		JLabel lblLine5 = new JLabel("2) this program is not integrated with or called by any product or service that generates revenue");
        		lblLine5.setBounds(10,90, 630, 14);
        		frmAbout.add(lblLine5);
           		
        		JLabel lblLine6 = new JLabel("3) your distribution of this program is free");
        		lblLine6.setBounds(10,110, 600, 14);
        		frmAbout.add(lblLine6);
           		
        		JLabel lblLine7 = new JLabel("Any modified versions of this program that are distributed or accessible shall indicate");
        		lblLine7.setBounds(10,150, 600, 14);
        		frmAbout.add(lblLine7);
           		
        		JLabel lblLine8 = new JLabel("that they are based on this program.  Educational institutions are granted permission");
        		lblLine8.setBounds(10,170, 600, 14);
        		frmAbout.add(lblLine8);
          		
        		JLabel lblLine9 = new JLabel("to distribute this program to their students and staff for a fee to recover distribution costs.");
        		lblLine9.setBounds(10,190, 600, 14);
        		frmAbout.add(lblLine9);
          		
        		JLabel lblLine10 = new JLabel("Permission requests for any other distribution of this program should be directed to:");
        		lblLine10.setBounds(10,210, 600, 14);
        		frmAbout.add(lblLine10);
          		
        		JLabel lblLine11 = new JLabel("license (at) u.washington.edu.");
        		lblLine11.setBounds(10,230, 600, 14);
        		frmAbout.add(lblLine11);
      		
        		btnOK = new JButton("OK");
        		btnOK.setBounds(500, 250, 84, 25);
        		btnOK.addActionListener(new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				frmAbout.dispose();
    					frmPopG.repaint();        					
       			}
        		});
        		frmAbout.add(btnOK);
        	}
          });
        mnFile.add(mntmAbout);
        
        
        frmPopG.setVisible(true);

        mntmQuit = new JMenuItem("Quit");
        mntmQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        mnFile.add(mntmQuit);
        frmPopG.setVisible(true);        
	}
	
	public boolean checkInputVals() {
		
		String msg;
		if(inputvals.popSize < 1) {
			msg = "Population size must be between 1 and 10000";
			JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if(inputvals.popSize > 10000) {
			msg = "Population size must be between 1 and 10000";
			JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if(inputvals.fitGenAA < 0) {
			msg = "Fitness of AA must be greater than or equal to 0";
			JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if(inputvals.fitGenAa < 0) {
			msg = "Fitness of Aa must be greater than or equal to 0";
			JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if(inputvals.fitGenaa < 0) {
			msg = "Fitness of aa must be greater than or equal to 0";
			JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if(inputvals.mutAa < 0) {
			msg = "Mutation rate from A to a must be between 0 and 1";
			JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if(inputvals.mutaA > 1) {
			msg = "Mutation rate from A to a must be between 0 and 1";
			JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if(inputvals.initFreq < 0) {
			msg = "The Initial frequency of allele A must be between 0 and 1";
			JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if(inputvals.initFreq > 1) {
			msg = "The Initial frequency of allele A must be between 0 and 1";
			JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if(inputvals.genRun < 1) {
			msg = "Generations to run must be between 1 and 100000000";
			JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if(inputvals.genRun > 100000000) {
			msg = "Generations to run must be between 1 and 100000000";
			JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if(inputvals.numPop < 0 ) {
			msg = "Number of populations must be between 0 and 1000";
			JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if(inputvals.numPop > 1000) {
			msg = "Number of populations must be between 0 and 1000";
			JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (inputvals.migRate < 0) {
			msg = "Migration rate cannot be negative";
			JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
			
		}
		return true;
	}

	
	protected void runPopGThreads(final Boolean newRun, final Integer genLim) { 

  	    Thread popgRunThread = new Thread() {
  	    	public void run() {
 	    		calcPopG(newRun, genLim);
	    	}
	  	};
  	  	popgRunThread.start();
  	  	
		timer = new Timer(TIMELEN, this);
		timer.addActionListener(this);
		timer.start();
 	}
	
	public void actionPerformed(ActionEvent e){
		displayPanel.repaint();
	}
	
	void calcPopG(Boolean newRun, Integer genLim) {
		// random number test case
		/*
		numberSeed = new Random();	
		genArray.clear();
		ArrayList<Double> popArray;
		for(int i = 0; i < inputvals.genRun; i++){
			popArray = new ArrayList<Double>(); 
			for(int j = 0; j <= inputvals.numPop; j++) {
				//popArray.add((double) numberSeed.nextFloat());
				double val = (double) numberSeed.nextFloat();
				popArray.add(val);
				//System.out.println("i: "+ i +" j: "+ j +" val: "+ val);
			}
			genArray.add(popArray);
		}
		*/
		
		ArrayList<Double> nextPopArray = new ArrayList<Double>();
		if (newRun)
		{
			genArray.clear();
			for(int j = 0; j <= inputvals.numPop; j++) {
				nextPopArray.add(inputvals.initFreq);
			}
			genArray.add(nextPopArray);
			beginGen = 0;
			endGen = inputvals.genRun;
		}
		else
		{
			beginGen = endGen;
			endGen += genLim;			
		}

		int currentPopSize = inputvals.popSize * 2;
		
		double pbar;
		double p = 0.0;
		double q;
		double w; 
		double pp1; 
		double pp2;
		
		long nx; 
		long ny;

		for(int i = beginGen; i < endGen; i++) {
			// calculate the mean frequency of AA over all the populations
			numFixedPops = 0;
			numLostPops = 0;
			pbar = 0.0;
			nextPopArray = new ArrayList<Double>();
			
			// count number of populations no longer active
			ArrayList<Double> popArray = genArray.get(i);
			for(int j = 1; j <= inputvals.numPop; j++) {
				double end = popArray.get(j);
				pbar += end;
				if(end <= 0.0) {
					numLostPops += 1;
				}
				if(end >= 1.0) {
					numFixedPops += 1;
				}
			}
			
			//for each population _________
			pbar /= inputvals.numPop; 
			for(int j = 0; j <= inputvals.numPop; j++) { //get new frequency for population j
				p =  popArray.get(j); //current gene frequency
				if(j > 0) {
					p = p * (1.0 - inputvals.migRate) + inputvals.migRate * pbar; //all but pop. 0 receive migrants
				}
				p = (1 - inputvals.mutAa) * p + inputvals.mutaA * (1 - p);  
				//if genotype is not fixed calculate the new frequency
				if((p > 0.0) && (p < 1.0)) {
					q = 1 - p;
					w = (p * p * inputvals.fitGenAA) + (2.0 * p * q * inputvals.fitGenAa) + (q * q * inputvals.fitGenaa); //get mean fitness
					pp1 = (p * p * inputvals.fitGenAA) / w; //get frequency of AA after sel.
					pp2 = (2.0 * p * q * inputvals.fitGenAa) / w; //get frequency of Aa after sel.
					if(j > 0) { //calculate the next generation
						nx = binomial(inputvals.popSize, pp1);
						//draw how many AA's survive
						if(pp1 < 1.0 && nx < inputvals.popSize) {
							ny = binomial((inputvals.popSize - nx), (pp2 / (1.0 - pp1))); //and Aa's too
						}
						else {
							ny = 0;
						}
						//compute new number of A's and make it a frequency
						nextPopArray.add(((nx *2.0) + ny) / currentPopSize); 
					}
					else { //calculate what the "true" average would be. What you would expect with an infinite population
						nextPopArray.add(pp1 + (pp2 / 2.0));
					}
				}
				else{
					if (p<=0.0){
						p = 0.0;
					}
					else
					{
						p = 1.0;
					}
					nextPopArray.add(p); // so we don't loose a population and mess up the plot
				}
			}
			genArray.add(nextPopArray);
		}
		
		// shut timer off and display the last of the data 
		timer.stop();
		//frmPopG.repaint();
		displayPanel.repaint();

	}

	long binomial(long n, double pp) {
		/*
		 * binomial distribution
		 * Parameters:
		 * 	long n:       number of trials
		 *  Double pp:    the probability of heads per trial
		 * Return value : the number of "heads"
		 */
		long j;
		long bnl;
		bnl = 0;
		for(j = 1; j <= n; j++) {
			if(numberSeed.nextFloat() < pp) {
				bnl++;
			}
		}
		return bnl;	
	}
	 
	void initInputVals(){
		inputvals.popSize = 100;
		inputvals.fitGenAA = 1.0;
		inputvals.fitGenAa = 1.0;
		inputvals.fitGenaa = 1.0;
		inputvals.mutAa = 0.0;
		inputvals.mutaA = 0.0;
		inputvals.migRate = 0.0;
		inputvals.initFreq = 0.5;
		inputvals.genRun = 100;
		inputvals.numPop = 10;
		inputvals.genSeed = true;
		inputvals.randSeed = (long) 0;
	}	 

	public void printPopG() {
		PrinterJob pj = PrinterJob.getPrinterJob(); 
	    Book book = new Book();
	    PageFormat documentPageFormat = new PageFormat();
	    documentPageFormat.setOrientation(PageFormat.LANDSCAPE);
	    book.append(new Document(), documentPageFormat);
	    pj.setPageable(book);
	    if (pj.printDialog()) {
	        try {
	        	pj.print();
        	} catch (Exception PrintException) {
        		PrintException.printStackTrace();
	        }
	      }
	}
	
	private class Document implements Printable {
		public int print(Graphics g, PageFormat pageFormat, int page) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
			xOffset = 100;
			yOffset = 40;
			xLen = (int)pageFormat.getImageableWidth() - 200;
			yLen = (int)pageFormat.getImageableHeight() - 130;
			Font titleFont = new Font("helvetica", Font.BOLD, 18);
			g2d.setFont(titleFont);
			g2d.drawString("PopG plot", xOffset + 150, yOffset - 20);
			plotPopG(g2d);
	      return (PAGE_EXISTS);
	    }
	}	
	
	public void savePopG() {
	    JFileChooser filechooser = new JFileChooser(filedir);
	    filechooser.setSelectedFile(new File("popg"));

	    // set up filter
	    filechooser.setAcceptAllFileFilterUsed(false);
	    FileNameExtensionFilter jpgfilter = new FileNameExtensionFilter("JPG Image", "jpg");
	    FileNameExtensionFilter pngfilter = new FileNameExtensionFilter("PNG Image", "png");
	    //FileNameExtensionFilter psfilter = new FileNameExtensionFilter("PS File", "ps");
	    filechooser.addChoosableFileFilter(jpgfilter);
	    filechooser.addChoosableFileFilter(pngfilter);
	    //filechooser.addChoosableFileFilter(psfilter);
	   
		int result = filechooser.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
		   filedir = filechooser.getCurrentDirectory().getAbsolutePath();
		   BufferedImage bi = new BufferedImage(frmPopG.getWidth(), frmPopG.getHeight(), BufferedImage.TYPE_INT_RGB);
		   Graphics2D g2d = bi.createGraphics();
		   g2d.setColor(Color.WHITE);
		   g2d.fillRect(0, 0, bi.getWidth(), bi.getHeight());
		   g2d.setColor(Color.BLACK);
		   xOffset = 100;
		   yOffset = 40;
		   xLen = frmPopG.getWidth() - 200;
		   yLen = frmPopG.getHeight() - 130;
		   Font titleFont = new Font("helvetica", Font.BOLD, 18);
		   g2d.setFont(titleFont);
		   g2d.drawString("PopG plot", xOffset + 150, yOffset - 20);
		   plotPopG(g2d);
		   
		   String curpath = filechooser.getSelectedFile().getPath();
		   FileFilter ff = filechooser.getFileFilter();
		   FileNameExtensionFilter extFilter = (FileNameExtensionFilter)ff;
		   String ext = extFilter.getExtensions()[0];
		   String fullpath;
		   if (curpath.contains("." + ext))
		   {
			   fullpath = curpath;
		   }
		   else
		   {
			   fullpath = curpath + "." + ext;
		   }
		   File saveFile =  new File(fullpath);
		   if (!saveFile.exists())
		   {
			   try {
				   saveFile.createNewFile();			   
			   } catch (IOException e1) {
				   System.out.println("Cannot create: " + fullpath);
				   return;
			   }
			}
	     
		   if (filechooser.getFileFilter() == jpgfilter)
		   {
		       try {
		           ImageIO.write(bi, "jpg", saveFile);
		       } catch (IOException e) {
		           e.printStackTrace();
		       } 
		   }
		   else //if (filechooser.getFileFilter() == pngfilter)
		   {
		       try {
		           ImageIO.write(bi, "png", saveFile);
		       } catch (IOException e) {
		           e.printStackTrace();
		       } 
		   }
		   /*
		   else //if (filechooser.getFileFilter() == psfilter)
		   {
		   	   // this will need to call explicit postscript writing code (probably cloned from old PopG code)
		   }
		   */
		}
	}

	void draw(Graphics g) {
	    Graphics2D g2d = (Graphics2D) g;
		xOffset = 100;
		yOffset = 20;
		xLen = frmPopG.getWidth() - 200;
		yLen = frmPopG.getHeight() - 130;
		g2d.setColor(getBackground());
		g2d.fillRect(0, 0, frmPopG.getWidth(), frmPopG.getHeight());
		g2d.setColor(Color.BLACK);

		plotPopG(g2d);
	}
	
	void plotPopG(Graphics2D g2d){
		BasicStroke stroke1 = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] {4.0f, 4.0f}, 0.0f);
		BasicStroke stroke2 = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f);
		BasicStroke stroke3 = new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f);
	    Font plotFont = new Font("helvetica", Font.PLAIN, 14);
	    g2d.setFont(plotFont);
			
		// draw axes		
		g2d.setStroke(stroke1);
		g2d.drawLine(xOffset, yOffset, xOffset + xLen, yOffset); // top x axis
		g2d.drawLine(xOffset, yOffset + yLen, xOffset + xLen, yOffset + yLen); // bottom x axis
		
		g2d.setStroke(stroke2);
		g2d.drawLine(xOffset, yOffset, xOffset, yOffset + yLen); // y axis
		
		int minX;
		int maxX;
		
		if(!genArray.isEmpty()) 
		{
			minX = beginGen;				
			maxX = endGen;
		}
		else
		{
			minX = 0;
			maxX =  inputvals.genRun;
		}
		int xAxisLen = maxX - minX;
		
		int labelDiv;
		if (xAxisLen < 5)
		{
			labelDiv = xAxisLen;
		}
		else
		{
			labelDiv = 5;
		}
		int xLbl = xAxisLen/labelDiv;
		int xLabelTweak = 8;

		// draw ticks and labels on x axis
		int lblpos = 0;
		while (lblpos <= xAxisLen)
		{
			int tickXCor = (int)((double)xLen/xAxisLen * lblpos);
			g2d.drawLine(xOffset + tickXCor, yOffset + yLen - 7,  xOffset + tickXCor,  yOffset + yLen + 7);
			g2d.drawString(String.valueOf(minX + lblpos),  xOffset + tickXCor - xLabelTweak,  yOffset + yLen + 23);
			lblpos += xLbl;
		}
		
		if ((xLbl > 1) && ((minX + lblpos - xLbl) != maxX))
		{
			int xRmdr = (maxX - minX)%5;
			if (xRmdr != 0)
			{
				// display last x label below
				g2d.drawLine( xOffset + xLen, yOffset + yLen - 7,  xOffset + xLen,  yOffset + yLen + 7);
				g2d.drawString(String.valueOf(maxX),  xOffset + xLen - xLabelTweak,  yOffset + yLen + 34);
				
			}
		}

		// draw ticks and labels on y axis
		int yLabelTweak = 5;
		for (int i = 0; i<11; i++)
		{
			g2d.drawLine(xOffset - 12, yOffset + (int)(i*yLen / 10), xOffset, yOffset + (int)(i*yLen / 10));
			g2d.drawString(String.format("%.1f", (double)(10 - i) * 0.1), xOffset - 35, yOffset + (int)(i*yLen / 10) + yLabelTweak);
		}
		
		// write the labels
		g2d.drawString("P(A)", xOffset/2 - 3*xLabelTweak, yOffset + (int)(5*yLen / 10) + yLabelTweak);
		g2d.drawString("Generation", xOffset + (int)(2*xLen/5) + xLabelTweak, yOffset + yLen + 40);
		String msg = "Fixed: " + numFixedPops;
		g2d.drawString(msg, xOffset + xLen + 10, yOffset + yLabelTweak);
		msg = "Lost: " + numLostPops;
		g2d.drawString(msg,  xOffset + xLen + 10, yOffset + yLen +yLabelTweak);

		// translate data into screen coordinates and display
		ArrayList<Double> popArray = new ArrayList<Double>();
		int startXCor;
		int startYCor;
		int endYCor;

		if(!genArray.isEmpty()) {		
			int beginXCor = xOffset;
			int endXCor = beginXCor;
			int beginYCor = ((int) (yLen * (1 - inputvals.initFreq))) + yOffset;;
			
			int firstlen = genArray.get(0).size();
			// skip j = 0 because it is rendered last so it will be on top
			for(int j = 1; j <= inputvals.numPop; j++) {
				
				startXCor = beginXCor;
				startYCor = beginYCor;
					
				for(int i = minX; i <= maxX; i++){
					if (i < genArray.size())
					{
						popArray = genArray.get(i);
						if((!popArray.isEmpty()) && (popArray.size() == firstlen)) {
							double yval = popArray.get(j);
							if (i == minX)
							{
								startXCor = xOffset;
								startYCor = ((int) (yLen * (1 - yval))) + yOffset;
							}
							else
							{
								g2d.setColor(Color.BLACK);
								g2d.setStroke(stroke2);
								endXCor = xOffset + (int)(((float)xLen/(float)xAxisLen) * (float)(i - minX));
								endYCor = ((int) (yLen * (1 - yval))) + yOffset;
								g2d.drawLine(startXCor, startYCor, endXCor,  endYCor);
								startXCor = endXCor;
								startYCor = endYCor;
							}
						}
					}
				}
			}
			
			// now render j==0 so it will be on top
			int j0startXCor = beginXCor;
			int j0startYCor = beginYCor;
			int j0endXCor;
			int j0endYCor;
			Color j0color = new Color(135,206,235);
			for(int i = minX; i <= maxX; i++){
				if (i < genArray.size())
				{
					popArray = genArray.get(i);
					if(!popArray.isEmpty()) {
						double yval = popArray.get(0);
						if (i == minX)
						{
							j0startXCor = xOffset;
							j0startYCor = ((int) (yLen * (1 - yval))) + yOffset;
						}
						else
						{
							g2d.setColor(j0color);										
							g2d.setStroke(stroke3);
							j0endXCor = xOffset + (int)(((float)xLen/(float)xAxisLen) * (float)(i - minX));
							j0endYCor = ((int) (yLen * (1 - yval))) + yOffset;
							g2d.drawLine(j0startXCor, j0startYCor, j0endXCor,  j0endYCor);
							j0startXCor = j0endXCor;
							j0startYCor = j0endYCor;
						}
					}		
				}
			}
			
			// create legend
			int lnStartX = (int)(xLen/5 - 20);
			int lnStartY = yOffset + yLen + 34;
			g2d.setColor(Color.BLACK);
			g2d.setStroke(stroke2);
			g2d.drawLine(lnStartX, lnStartY, lnStartX + 20,  lnStartY);
			g2d.drawString("evolving populations", lnStartX + 30, lnStartY + 6);			
			lnStartY += 14;
			g2d.drawString("with no drift", lnStartX + 30, lnStartY + 6);
			g2d.setColor(j0color);										
			g2d.setStroke(stroke3);
			g2d.drawLine(lnStartX, lnStartY, lnStartX + 20,  lnStartY);
		}	
	}
}



