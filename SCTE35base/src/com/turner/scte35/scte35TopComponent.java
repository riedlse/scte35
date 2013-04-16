/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.turner.scte35;

import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.apache.commons.codec.binary.Base64;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//com.turner.scte35//scte35//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "scte35TopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = "Window", id = "com.turner.scte35.scte35TopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_scte35Action",
        preferredID = "scte35TopComponent")
@Messages({
    "CTL_scte35Action=scte35",
    "CTL_scte35TopComponent=scte35 Window",
    "HINT_scte35TopComponent=This is a scte35 window"
})
public final class scte35TopComponent extends TopComponent {

    private byte[] bbuffer = new byte[4096];
    private int bufsize;
    
    public scte35TopComponent() {
        initComponents();
        setName(Bundle.CTL_scte35TopComponent());
        setToolTipText(Bundle.HINT_scte35TopComponent());

        switch (spliceCommandType.getSelectedIndex()) {
            case 0:
            case 1:
            case 2:
            case 3:
                timeSignal.setVisible(false);
                break;
            case 4:
                timeSignal.setVisible(false);
                break;
            case 5:
                timeSignal.setVisible(false);
                break;
            case 6:
                timeSignal.setVisible(true);
                break;
            case 7:
                timeSignal.setVisible(false);
                break;
            case 8:
                timeSignal.setVisible(false);
                break;
        }
        switch (descriptor1.getSelectedIndex()) {
            case 0: segDesc1.setVisible(false);
                break;
            case 1: segDesc1.setVisible(false);
                break;
            case 2: segDesc1.setVisible(true);
                break;
            case 3: segDesc1.setVisible(false);
                break;   
        }
        
        fillBuffer();
        displayBuffer();
        
    }

    public void fillBuffer() {
        bufsize = 0;
        bbuffer[bufsize] = (byte) 0xFC; // table id
        bufsize++;
        bbuffer[bufsize] = (byte) 0x30; // ssi/ppi/1/1 length
        bufsize++;
        bbuffer[bufsize] = (byte) 0x00; // length
        bufsize++;
        bbuffer[bufsize] = (byte) 0x00; // protocol
        bufsize++;
        bbuffer[bufsize] = (byte) 0x00; // encryption / pts adjust
        bufsize++;
        bbuffer[bufsize] = (byte) 0x00; //pts
        bufsize++;
        bbuffer[bufsize] = (byte) 0x00; //pts
        bufsize++;
        bbuffer[bufsize] = (byte) 0x00; //pts
        bufsize++;
        bbuffer[bufsize] = (byte) 0x00; //pts
        bufsize++;
        bbuffer[bufsize] = (byte) 0x00; // CW index
        bufsize++;
        
        int itier = tier.getSelectedIndex();
        
        bbuffer[bufsize] = (byte) ((itier >> 4) & 0x00FF); // tier MSB
        bufsize++;
        bbuffer[bufsize] = (byte) ((itier << 4) & 0x00F0); // Tier/command length
        bufsize++;
        bbuffer[bufsize] = (byte) 0x00; // command length 
        bufsize++;

        int isct = spliceCommandType.getSelectedIndex();
        
        switch (isct) {
            case 0: 
            case 1:
            case 2:
            case 3:
                break;
            case 4: insSpliceSchedule();
                break;
            case 5: insSpliceInsert();
                break;
            case 6: insTimeSignal();
                break;
            case 7: insBandwidthReservation();
                break;
            case 8: insPrivateCommand();
                break;
        }
        
        // insert descriptor loop
        int desclenptr = bufsize;
        bbuffer[bufsize] = (byte) 0x00; //descriptor loop length (fix later...)
        bufsize++;
        bbuffer[bufsize] = (byte) 0x00;
        bufsize++;
        int desclen = bufsize;
 
        switch (descriptor1.getSelectedIndex()) {
            case 0:
                insertAvailDesc1();
                break;
            case 1:
                insertDTMFdesc1();
                break;
            case 2:
                insertSegDesc1();
                break;
        }
        switch (descriptor2.getSelectedIndex()) {
            case 0:
                insertAvailDesc2();
                break;
            case 1:
                insertDTMFdesc2();
                break;
            case 2:
                insertSegDesc2();
                break;
        }
        switch (descriptor3.getSelectedIndex()) {
            case 0:
                insertAvailDesc3();
                break;
            case 1:
                insertDTMFdesc3();
                break;
            case 2:
                insertSegDesc3();
                break;
        }

        // fill in descriptor length
        int dlen = bufsize - desclen;
        bbuffer[desclenptr] = (byte) ((dlen >> 8) & 0x00FF); //descriptor loop length (fix later...)
        bbuffer[desclenptr + 1] = (byte) (dlen & 0x00FF);
        
        // fill in length (-3?? CRC???)
        bbuffer[1] = (byte) (0x30 | ((bufsize >>8) &0x0F)); // ssi/ppi/1/1 length
        bbuffer[2] = (byte) (bufsize & 0x00FF); // length
        
        //create CRC
    }
    
    public void insertSegDesc1() {
        bbuffer[bufsize] = (byte) 0x02;
        bufsize++;
        bbuffer[bufsize] = (byte) 0x00; // length calculate
        bufsize++;
        int seg1len = bufsize;
        bbuffer[bufsize] = (byte) 0x43;
        bufsize++;
        bbuffer[bufsize] = (byte) 0x55;
        bufsize++;
        bbuffer[bufsize] = (byte) 0x45;
        bufsize++;
        bbuffer[bufsize] = (byte) 0x49;
        bufsize++;
        int MSN = 0x00;
        switch (segEventMSN.getSelectedIndex()) {
            case 1 : MSN = 0x40;
                break;
            case 2 : MSN = 0x80;
                break;
            case 3 : MSN = 0xC0;
                break;
        }
        bbuffer[bufsize] = (byte) (MSN | (0x00 & 0x0F));
        bufsize++;
        bbuffer[bufsize] = (byte) 0x00;
        bufsize++;
        bbuffer[bufsize] = (byte) 0x00;
        bufsize++;
        bbuffer[bufsize] = (byte) 0x00;
        bufsize++;
 
        if (seci.isSelected()) {
            bbuffer[bufsize] = (byte) 0xFF;
            bufsize++;
        } else {
            bbuffer[bufsize] = (byte) 0x7F;
            bufsize++;
            byte btemp = 0x00;
            if (psf.isSelected()) {
                btemp |= 0x80;
            }
            if (sdf.isSelected()) {
                btemp |= 0x40;
            }
            if (dnrf.isSelected()) {
                btemp |= 0x3F;
            } else {
                if (wdaf.isSelected()) {
                    btemp |= 0x10;
                }
                if (nrbf.isSelected()) {
                    btemp |= 0x08;
                }
                if (aaf.isSelected()) {
                    btemp |= 0x04;
                }
                if (drmsb.isSelected()) {
                    btemp |= 0x02;
                }
                if (drlsb.isSelected()) {
                    btemp |= 0x01;
                }
                bbuffer[bufsize] = btemp;
                bufsize++;
            }
            if (!psf.isSelected()) {
                // don't support component mode....
            }
            if (sdf.isSelected()) {
                bbuffer[bufsize] = (byte) 0x00;
                bufsize++;
                bbuffer[bufsize] = (byte) 0x00;
                bufsize++;
                bbuffer[bufsize] = (byte) 0x00;
                bufsize++;
                bbuffer[bufsize] = (byte) 0x00;
                bufsize++;
                bbuffer[bufsize] = (byte) 0x00;
                bufsize++;
            }
            switch (upidType.getSelectedIndex()) {
                case 0:
                    bbuffer[bufsize] = (byte) 0x00;
                    bufsize++;
                    bbuffer[bufsize] = (byte) 0x00;
                    bufsize++;
                    break;
            }
            switch (typeId.getSelectedIndex()) {
                case 0:
            }
            bbuffer[bufsize] = (byte) 0x00; // Segnum
            bufsize++;
            bbuffer[bufsize] = (byte) 0x00; // SegExp
            bufsize++;
        }
    }
    
    public void insertSegDesc2() {
        
    }
    
    public void insertSegDesc3() {
        
    }
        
    public void insertDTMFdesc1() {
        
    }
    
    public void insertDTMFdesc2() {
        
    }
    
    public void insertDTMFdesc3() {
        
    }
    
    public void insertAvailDesc1() {
        
    }
    
    public void insertAvailDesc2() {
        
    }
    
    public void insertAvailDesc3() {
        
    }
    
    
    public void insSpliceSchedule() {
        bbuffer[bufsize] = (byte) 0x04;
        bufsize++;
        
    }
    
    public void insSpliceInsert() {
        bbuffer[bufsize] = (byte) 0x05;
        bufsize++;
        
    }
    
    public void insTimeSignal() {
        bbuffer[bufsize] = (byte) 0x06;
        bufsize++;
        
        if (timeSpecified.isSelected()) {
            String stemp = "0x123456789"; //ptsTime.toString();
            System.out.println(stemp);
            long pts = Long.decode(stemp);
            bbuffer[bufsize] = (byte) (((pts >> 32) & 0x01) | 0x7E);
            bufsize++;
            bbuffer[bufsize] = (byte) ((pts >> 24) & 0x00FF);
            bufsize++;
            bbuffer[bufsize] = (byte) ((pts >> 16) & 0x00FF);
            bufsize++;
            bbuffer[bufsize] = (byte) ((pts >> 8) & 0x00FF);
            bufsize++;
            bbuffer[bufsize] = (byte) (pts & 0x00FF);
            bufsize++;
        } else {
            bbuffer[bufsize] = (byte) 0x7F;
            bufsize++;
        }
    }

    public void insBandwidthReservation() {
        bbuffer[bufsize] = (byte) 0x07;
        bufsize++;
        
    }

    public void insPrivateCommand() {
        bbuffer[bufsize] = (byte) 0xFF;
        bufsize++;
        
    }

    public void displayBuffer() {
        byte[] dbuffer = new byte[bufsize];
        String stemp = "";
        for (int i=0;i<bufsize;i++) {
            stemp += String.format("%02X", bbuffer[i]);
            dbuffer[i] = bbuffer[i];
        }
        hex35.setText(stemp);    
        b6435.setText(Base64.encodeBase64String(dbuffer));
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        descriptor3 = new javax.swing.JComboBox();
        descriptor1 = new javax.swing.JComboBox();
        jLabel10 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        descriptor2 = new javax.swing.JComboBox();
        spliceCommandType = new javax.swing.JComboBox();
        jLabel31 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        tier = new javax.swing.JComboBox();
        jLabel22 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel37 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        b6435 = new javax.swing.JTextField();
        hex35 = new javax.swing.JTextField();
        timeSignal = new javax.swing.JPanel();
        jLabel38 = new javax.swing.JLabel();
        timeSpecified = new javax.swing.JCheckBox();
        ptsTime = new javax.swing.JTextField();
        segDesc1 = new javax.swing.JPanel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        segEventMSN = new javax.swing.JComboBox();
        segEventID = new javax.swing.JTextField();
        seci = new javax.swing.JCheckBox();
        psf = new javax.swing.JCheckBox();
        sdf = new javax.swing.JCheckBox();
        dnrf = new javax.swing.JCheckBox();
        wdaf = new javax.swing.JCheckBox();
        nrbf = new javax.swing.JCheckBox();
        aaf = new javax.swing.JCheckBox();
        drmsb = new javax.swing.JCheckBox();
        drlsb = new javax.swing.JCheckBox();
        jLabel46 = new javax.swing.JLabel();
        segDuration = new javax.swing.JTextField();
        upidType = new javax.swing.JComboBox();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        upidLength = new javax.swing.JTextField();
        jLabel49 = new javax.swing.JLabel();
        upid = new javax.swing.JTextField();
        jLabel50 = new javax.swing.JLabel();
        typeId = new javax.swing.JComboBox();
        jLabel51 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        segnum = new javax.swing.JTextField();
        segexp = new javax.swing.JTextField();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jPanel1.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel27, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel27.text")); // NOI18N

        descriptor3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "avail_descriptor", "DTMF_descriptor", "segmentation_descriptor", "Not Used" }));
        descriptor3.setSelectedIndex(3);
        descriptor3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                descriptor3ActionPerformed(evt);
            }
        });

        descriptor1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "avail_descriptor", "DTMF_descriptor", "segmentation_descriptor", "Not Used" }));
        descriptor1.setSelectedIndex(3);
        descriptor1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                descriptor1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel10.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel11.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel30, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel30.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel34, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel34.text")); // NOI18N

        descriptor2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "avail_descriptor", "DTMF_descriptor", "segmentation_descriptor", "Not Used" }));
        descriptor2.setSelectedIndex(3);
        descriptor2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                descriptor2ActionPerformed(evt);
            }
        });

        spliceCommandType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "splice_null", "Reserved", "Reserved", "Reserved", "splice_schedule", "splice_insert", "time_signal", "bandwidth_reservation", "private_command" }));
        spliceCommandType.setSelectedIndex(6);
        spliceCommandType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spliceCommandTypeActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel31, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel31.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel21, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel21.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel16, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel16.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel6.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel23, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel23.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel20, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel20.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel17, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel17.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel9.text")); // NOI18N

        tier.setMaximumRowCount(10);
        tier.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" }));
        tier.setSelectedIndex(1);
        tier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tierActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel22, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel22.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel28, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel28.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel26, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel26.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel12.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel7.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel24, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel24.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel32, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel32.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel25, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel25.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel29, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel29.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel35, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel35.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel14, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel14.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel18, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel18.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel15, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel15.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel19, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel19.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel8.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel13, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel13.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel33, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel33.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel7)
                    .addComponent(jLabel9)
                    .addComponent(jLabel11)
                    .addComponent(jLabel12)
                    .addComponent(jLabel13)
                    .addComponent(jLabel14)
                    .addComponent(jLabel15)
                    .addComponent(jLabel16)
                    .addComponent(jLabel17)
                    .addComponent(jLabel18)
                    .addComponent(jLabel25)
                    .addComponent(jLabel27)
                    .addComponent(jLabel29)
                    .addComponent(jLabel30)
                    .addComponent(jLabel31)
                    .addComponent(jLabel32)
                    .addComponent(jLabel34))
                .addGap(23, 23, 23)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel35)
                    .addComponent(jLabel33)
                    .addComponent(descriptor3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(descriptor2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(descriptor1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel28)
                    .addComponent(jLabel26)
                    .addComponent(spliceCommandType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24)
                    .addComponent(tier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23)
                    .addComponent(jLabel22)
                    .addComponent(jLabel21)
                    .addComponent(jLabel20)
                    .addComponent(jLabel19)
                    .addComponent(jLabel10)
                    .addComponent(jLabel8)
                    .addComponent(jLabel6)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jLabel19))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jLabel20))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(jLabel21))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(jLabel22))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jLabel23))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(tier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(jLabel24))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(spliceCommandType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(jLabel26))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(jLabel28))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(descriptor1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel30)
                    .addComponent(descriptor2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel31)
                    .addComponent(descriptor3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel32)
                    .addComponent(jLabel33))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel34)
                    .addComponent(jLabel35))
                .addContainerGap())
        );

        org.openide.awt.Mnemonics.setLocalizedText(jLabel37, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel37.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel36, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel36.text")); // NOI18N

        b6435.setText(org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.b6435.text")); // NOI18N

        hex35.setText(org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.hex35.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel36)
                    .addComponent(jLabel37))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(b6435, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hex35, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(200, 200, 200))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel37)
                    .addComponent(hex35, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel36)
                    .addComponent(b6435, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        timeSignal.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.timeSignal.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel38, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel38.text")); // NOI18N

        timeSpecified.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(timeSpecified, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.timeSpecified.text")); // NOI18N
        timeSpecified.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timeSpecifiedActionPerformed(evt);
            }
        });

        ptsTime.setText(org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.ptsTime.text")); // NOI18N
        ptsTime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ptsTimeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout timeSignalLayout = new javax.swing.GroupLayout(timeSignal);
        timeSignal.setLayout(timeSignalLayout);
        timeSignalLayout.setHorizontalGroup(
            timeSignalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(timeSignalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel38)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ptsTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(timeSignalLayout.createSequentialGroup()
                .addComponent(timeSpecified)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        timeSignalLayout.setVerticalGroup(
            timeSignalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(timeSignalLayout.createSequentialGroup()
                .addComponent(timeSpecified)
                .addGap(3, 3, 3)
                .addGroup(timeSignalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel38)
                    .addComponent(ptsTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        segDesc1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.segDesc1.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel39, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel39.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel40, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel40.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel41, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel41.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel42, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel42.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel43, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel43.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel44, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel44.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel45, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel45.text")); // NOI18N

        segEventMSN.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Source", "Automation", "Live", "Local" }));
        segEventMSN.setSelectedIndex(1);
        segEventMSN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                segEventMSNActionPerformed(evt);
            }
        });

        segEventID.setText(org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.segEventID.text")); // NOI18N
        segEventID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                segEventIDActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(seci, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.seci.text")); // NOI18N
        seci.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seciActionPerformed(evt);
            }
        });

        psf.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(psf, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.psf.text")); // NOI18N
        psf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                psfActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(sdf, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.sdf.text")); // NOI18N
        sdf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sdfActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(dnrf, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.dnrf.text")); // NOI18N
        dnrf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dnrfActionPerformed(evt);
            }
        });

        wdaf.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(wdaf, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.wdaf.text")); // NOI18N
        wdaf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wdafActionPerformed(evt);
            }
        });

        nrbf.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(nrbf, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.nrbf.text")); // NOI18N
        nrbf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nrbfActionPerformed(evt);
            }
        });

        aaf.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(aaf, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.aaf.text")); // NOI18N
        aaf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aafActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(drmsb, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.drmsb.text")); // NOI18N
        drmsb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                drmsbActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(drlsb, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.drlsb.text")); // NOI18N
        drlsb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                drlsbActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel46, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel46.text")); // NOI18N

        segDuration.setText(org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.segDuration.text")); // NOI18N
        segDuration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                segDurationActionPerformed(evt);
            }
        });

        upidType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Not Used", "User Defined (Dep)", "ISCI", "Ad-ID", "UMID", "ISAN (Dep)", "ISAN", "TID", "Turner", "ADI", "EIDR", "ATSC CI", "MPU", "MID" }));
        upidType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upidTypeActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel47, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel47.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel48, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel48.text")); // NOI18N

        upidLength.setText(org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.upidLength.text")); // NOI18N
        upidLength.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upidLengthActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel49, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel49.text")); // NOI18N

        upid.setText(org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.upid.text")); // NOI18N
        upid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upidActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel50, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel50.text")); // NOI18N

        typeId.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Not Indicated", "Content Identification", "Program Start", "Program End", "Program Early Termination", "Program Breakaway", "Program Resumption", "Program Runover Planned", "Program Runover Unplanned", "Program Overlap Start", "Chapter Start", "Chapter End", "Provider Advertisement Start", "ProviderAdvertisement End", "Distributor Advertisement Start", "Distributor Advertisement End", "Placement Opportunity Start", "Placement Opportunity End", "Unscheduled Event Start", "Unscheduled Event End", "Network Start", "Network End" }));
        typeId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeIdActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel51, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel51.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel52, org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.jLabel52.text")); // NOI18N

        segnum.setText(org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.segnum.text")); // NOI18N
        segnum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                segnumActionPerformed(evt);
            }
        });

        segexp.setText(org.openide.util.NbBundle.getMessage(scte35TopComponent.class, "scte35TopComponent.segexp.text")); // NOI18N
        segexp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                segexpActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout segDesc1Layout = new javax.swing.GroupLayout(segDesc1);
        segDesc1.setLayout(segDesc1Layout);
        segDesc1Layout.setHorizontalGroup(
            segDesc1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(segDesc1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(segDesc1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(segDesc1Layout.createSequentialGroup()
                        .addGroup(segDesc1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel39)
                            .addComponent(jLabel41)
                            .addComponent(jLabel43))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(segDesc1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel44)
                            .addComponent(jLabel42)
                            .addComponent(jLabel40)))
                    .addGroup(segDesc1Layout.createSequentialGroup()
                        .addComponent(jLabel45)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(segEventMSN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(segEventID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(seci)
                    .addComponent(psf)
                    .addComponent(sdf)
                    .addComponent(dnrf)
                    .addComponent(wdaf)
                    .addComponent(nrbf)
                    .addComponent(aaf)
                    .addGroup(segDesc1Layout.createSequentialGroup()
                        .addComponent(drmsb)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(drlsb))
                    .addGroup(segDesc1Layout.createSequentialGroup()
                        .addGroup(segDesc1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel46)
                            .addComponent(jLabel47))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(segDesc1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(upidType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(segDuration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(segDesc1Layout.createSequentialGroup()
                        .addGroup(segDesc1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel48)
                            .addComponent(jLabel49)
                            .addComponent(jLabel50))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(segDesc1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(typeId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(upid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(upidLength, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(segDesc1Layout.createSequentialGroup()
                        .addGroup(segDesc1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel52)
                            .addComponent(jLabel51))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(segDesc1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(segnum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(segexp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        segDesc1Layout.setVerticalGroup(
            segDesc1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(segDesc1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(segDesc1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel39)
                    .addComponent(jLabel40))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(segDesc1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel41)
                    .addComponent(jLabel42))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(segDesc1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel43)
                    .addComponent(jLabel44))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(segDesc1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel45)
                    .addComponent(segEventMSN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(segEventID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(seci)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(psf)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sdf)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dnrf)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(wdaf)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nrbf)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(aaf)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(segDesc1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(drmsb)
                    .addComponent(drlsb))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(segDesc1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel46)
                    .addComponent(segDuration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(segDesc1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel47)
                    .addComponent(upidType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(segDesc1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel48)
                    .addComponent(upidLength, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(segDesc1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel49)
                    .addComponent(upid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(segDesc1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel50)
                    .addComponent(typeId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(segDesc1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel51)
                    .addComponent(segnum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(segDesc1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel52)
                    .addComponent(segexp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(timeSignal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(segDesc1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(timeSignal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(segDesc1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tierActionPerformed
        fillBuffer();
        displayBuffer();
    }//GEN-LAST:event_tierActionPerformed

    private void spliceCommandTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spliceCommandTypeActionPerformed
        switch (spliceCommandType.getSelectedIndex()) {
            case 0:
            case 1:
            case 2:
            case 3:
                timeSignal.setVisible(false);
                break;
            case 4:
                timeSignal.setVisible(false);
                break;
            case 5:
                timeSignal.setVisible(false);
                break;
            case 6:
                timeSignal.setVisible(true);
                break;
            case 7:
                timeSignal.setVisible(false);
                break;
            case 8:
                timeSignal.setVisible(false);
                break;
        }
        
        fillBuffer();
        displayBuffer();
    }//GEN-LAST:event_spliceCommandTypeActionPerformed

    private void descriptor1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_descriptor1ActionPerformed
        switch (descriptor1.getSelectedIndex()) {
            case 0: segDesc1.setVisible(false);
                break;
            case 1: segDesc1.setVisible(false);
                break;
            case 2: segDesc1.setVisible(true);
                break;
            case 3: segDesc1.setVisible(false);
                break;   
        }
        fillBuffer();
        displayBuffer();
    }//GEN-LAST:event_descriptor1ActionPerformed

    private void descriptor2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_descriptor2ActionPerformed
        fillBuffer();
        displayBuffer();
        // TODO add your handling code here:
    }//GEN-LAST:event_descriptor2ActionPerformed

    private void descriptor3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_descriptor3ActionPerformed
        fillBuffer();
        displayBuffer();
        // TODO add your handling code here:
    }//GEN-LAST:event_descriptor3ActionPerformed

    private void timeSpecifiedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timeSpecifiedActionPerformed
        fillBuffer();
        displayBuffer();
        // TODO add your handling code here:
    }//GEN-LAST:event_timeSpecifiedActionPerformed

    private void ptsTimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ptsTimeActionPerformed
        fillBuffer();
        displayBuffer();
        // TODO add your handling code here:
    }//GEN-LAST:event_ptsTimeActionPerformed

    private void wdafActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wdafActionPerformed
        fillBuffer();
        displayBuffer();
        // TODO add your handling code here:
    }//GEN-LAST:event_wdafActionPerformed

    private void segEventMSNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_segEventMSNActionPerformed
        fillBuffer();
        displayBuffer();
        // TODO add your handling code here:
    }//GEN-LAST:event_segEventMSNActionPerformed

    private void segEventIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_segEventIDActionPerformed
        fillBuffer();
        displayBuffer();
        // TODO add your handling code here:
    }//GEN-LAST:event_segEventIDActionPerformed

    private void seciActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seciActionPerformed
        fillBuffer();
        displayBuffer();
        // TODO add your handling code here:
    }//GEN-LAST:event_seciActionPerformed

    private void psfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_psfActionPerformed
        fillBuffer();
        displayBuffer();
        // TODO add your handling code here:
    }//GEN-LAST:event_psfActionPerformed

    private void sdfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sdfActionPerformed
        fillBuffer();
        displayBuffer();
        // TODO add your handling code here:
    }//GEN-LAST:event_sdfActionPerformed

    private void dnrfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dnrfActionPerformed
        fillBuffer();
        displayBuffer();
        // TODO add your handling code here:
    }//GEN-LAST:event_dnrfActionPerformed

    private void nrbfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nrbfActionPerformed
        fillBuffer();
        displayBuffer();
        // TODO add your handling code here:
    }//GEN-LAST:event_nrbfActionPerformed

    private void aafActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aafActionPerformed
        fillBuffer();
        displayBuffer();
        // TODO add your handling code here:
    }//GEN-LAST:event_aafActionPerformed

    private void drmsbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drmsbActionPerformed
        fillBuffer();
        displayBuffer();
        // TODO add your handling code here:
    }//GEN-LAST:event_drmsbActionPerformed

    private void drlsbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drlsbActionPerformed
        fillBuffer();
        displayBuffer();
        // TODO add your handling code here:
    }//GEN-LAST:event_drlsbActionPerformed

    private void segDurationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_segDurationActionPerformed
        fillBuffer();
        displayBuffer();
        // TODO add your handling code here:
    }//GEN-LAST:event_segDurationActionPerformed

    private void upidTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upidTypeActionPerformed
        fillBuffer();
        displayBuffer();
        // TODO add your handling code here:
    }//GEN-LAST:event_upidTypeActionPerformed

    private void upidLengthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upidLengthActionPerformed
        fillBuffer();
        displayBuffer();
        // TODO add your handling code here:
    }//GEN-LAST:event_upidLengthActionPerformed

    private void upidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upidActionPerformed
        fillBuffer();
        displayBuffer();
        // TODO add your handling code here:
    }//GEN-LAST:event_upidActionPerformed

    private void typeIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeIdActionPerformed
        fillBuffer();
        displayBuffer();
    }//GEN-LAST:event_typeIdActionPerformed

    private void segnumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_segnumActionPerformed
        fillBuffer();
        displayBuffer();
    }//GEN-LAST:event_segnumActionPerformed

    private void segexpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_segexpActionPerformed
        fillBuffer();
        displayBuffer();
    }//GEN-LAST:event_segexpActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox aaf;
    private javax.swing.JTextField b6435;
    private javax.swing.JComboBox descriptor1;
    private javax.swing.JComboBox descriptor2;
    private javax.swing.JComboBox descriptor3;
    private javax.swing.JCheckBox dnrf;
    private javax.swing.JCheckBox drlsb;
    private javax.swing.JCheckBox drmsb;
    private javax.swing.JTextField hex35;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JCheckBox nrbf;
    private javax.swing.JCheckBox psf;
    private javax.swing.JTextField ptsTime;
    private javax.swing.JCheckBox sdf;
    private javax.swing.JCheckBox seci;
    private javax.swing.JPanel segDesc1;
    private javax.swing.JTextField segDuration;
    private javax.swing.JTextField segEventID;
    private javax.swing.JComboBox segEventMSN;
    private javax.swing.JTextField segexp;
    private javax.swing.JTextField segnum;
    private javax.swing.JComboBox spliceCommandType;
    private javax.swing.JComboBox tier;
    private javax.swing.JPanel timeSignal;
    private javax.swing.JCheckBox timeSpecified;
    private javax.swing.JComboBox typeId;
    private javax.swing.JTextField upid;
    private javax.swing.JTextField upidLength;
    private javax.swing.JComboBox upidType;
    private javax.swing.JCheckBox wdaf;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
