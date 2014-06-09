package net.authorize.data.aim;

import java.math.BigDecimal;

/**
 * Container used in split tender transactions.
 * 
 */
public class PrepaidCard {

  private BigDecimal requestedAmount = null;
  private BigDecimal approvedAmount  = null;
  private BigDecimal balanceOnCard   = null;

  private PrepaidCard() {
  }

  public static PrepaidCard createPrepaidCard() {
    return new PrepaidCard();
  }

  /**
   * @return the requestedAmount
   */
  public BigDecimal getRequestedAmount() {
    return requestedAmount;
  }

  /**
   * @param requestedAmount
   *          the requestedAmount to set
   */
  public void setRequestedAmount(BigDecimal requestedAmount) {
    this.requestedAmount = requestedAmount;
  }

  /**
   * @return the approvedAmount
   */
  public BigDecimal getApprovedAmount() {
    return approvedAmount;
  }

  /**
   * @param approvedAmount
   *          the approvedAmount to set
   */
  public void setApprovedAmount(BigDecimal approvedAmount) {
    this.approvedAmount = approvedAmount;
  }

  /**
   * @return the balanceOnCard
   */
  public BigDecimal getBalanceOnCard() {
    return balanceOnCard;
  }

  /**
   * @param balanceOnCard
   *          the balanceOnCard to set
   */
  public void setBalanceOnCard(BigDecimal balanceOnCard) {
    this.balanceOnCard = balanceOnCard;
  }

}
