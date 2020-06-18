from datetime import datetime
import email
import json
import imaplib
import logging
import pprint
import re
import smtplib
import sys

TIME_PATTERN = '%H:%M:%S'
DATE_PATTERN_SEARCH_CRIT = '%d-%b-%Y'
DATETIME_PATTERN_SEARCH_CRIT = DATE_PATTERN_SEARCH_CRIT + ' ' + TIME_PATTERN

logging.basicConfig(filename='mailforwarder.log', level=logging.INFO, format='[%(levelname)s] %(asctime)s %(message)s')

def read_cfg(filename):
    cfg = {}
    with open(filename) as cfgfile:
        cfg = json.load(cfgfile)
        cfgfile.close()
    return cfg

def write_cfg(filename, cfg):
    with open(filename, "w") as cfgfile:
        json.dump(cfg, cfgfile, indent=4)
        cfgfile.close()

def getMessageDateTime(datetimeString):
    splitted = datetimeString.split()
    str = splitted[1] + '-' + splitted[2] + '-' + splitted[3] + ' ' + splitted[4]
    return datetime.strptime(str, DATETIME_PATTERN_SEARCH_CRIT)

def process_account(mailAccount, forwardAddress, fromAddress, smtpAccount, simulate):
    logging.info("Processing mail account %s (Simulate %s)", mailAccount['user'], simulate)
    imap = imaplib.IMAP4_SSL(mailAccount['host'])
    imap.login(mailAccount['user'], mailAccount['password'])
    imap.select(mailAccount['folder'])
    not_before_datetime = datetime.strptime(mailAccount['timestamp'], DATETIME_PATTERN_SEARCH_CRIT)
    tmp, data = imap.search(None, '(NOT BEFORE "' + not_before_datetime.strftime(DATE_PATTERN_SEARCH_CRIT) + '")')
    latest_date = not_before_datetime
    
    for num in data[0].split():
        tmp, data = imap.fetch(num, 'RFC822')
        message = email.message_from_string(data[0][1].decode('UTF-8'))
        message_date = getMessageDateTime(message.get('Date'))
        if message_date > not_before_datetime:
            if message_date > latest_date:
                latest_date = message_date
            forward_message(message, forwardAddress, fromAddress, smtpAccount, simulate)
    imap.close()
    imap.logout()
    mailAccount['timestamp'] = latest_date.strftime(DATETIME_PATTERN_SEARCH_CRIT)

def forward_message(message, forwardAddress, fromAddress, smtpAccount, simulate):
    subject = message.get('Subject') + " [" + message.get('From') + " -> " + message.get('To') + "]"
    date = getMessageDateTime(message.get('Date'))
    logmsg = ' \'{}\' ({})'.format(subject, date.strftime('%d-%b-%Y %H:%M:%S'))
    if not simulate:
        try:
            message.replace_header('Subject', subject)
            message.replace_header('From', fromAddress)
            message.replace_header('To', forwardAddress)
            if 'Cc' in message:
                message.replace_header('Cc', '')
            if 'Bcc' in message:
                message.replace_header('Bcc', '')
            logging.debug("Connecting...")
            smtp = smtplib.SMTP(smtpAccount['host'], smtpAccount['port'])
            smtp.starttls()
            logging.debug("Login...")
            smtp.login(smtpAccount['user'], smtpAccount['password'])
            logging.debug("Sending mail...")
            smtp.sendmail(fromAddress, forwardAddress, message.as_string())
            logging.debug("Close connection")
            smtp.quit()
            logging.info('OK' + logmsg)
        except:
            logging.error('NOK' + logmsg)
            logging.error(sys.exc_info())
    else:
        logging.info('OK' + logmsg)
    
if __name__ == "__main__":
    cfg = read_cfg('mailforwardercfg.json')
    for mailAccount in cfg['mailAccounts']:
        process_account(mailAccount, cfg['forwardAddress'], cfg['fromAddress'], cfg['smtpAccount'], cfg['simulate'])
    write_cfg('mailforwardercfg.json', cfg)
    

